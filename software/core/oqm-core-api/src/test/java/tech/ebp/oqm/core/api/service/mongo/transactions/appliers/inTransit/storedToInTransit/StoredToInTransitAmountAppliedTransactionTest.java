package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.storedToInTransit;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.exception.db.DbDeletedException;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.storedToInTransit.StoredToInTransitAmountTransaction;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.InTransitAppliedTransactionTest;
import tech.units.indriya.quantity.Quantities;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.ebp.oqm.core.api.model.object.history.details.HistoryDetailType.ITEM_TRANSACTION;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
@QuarkusTestResource(value = KafkaCompanionResource.class, restrictToAnnotatedClass = true)
public class StoredToInTransitAmountAppliedTransactionTest extends InTransitAppliedTransactionTest {



	@Test
	public void applyAddAmountInTransitSuccessAmountListPart() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlockIds().findFirst().get())
														.build();
		AppliedTransaction initialAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		preApplyTransaction = StoredToInTransitAmountTransaction.builder()
								  .subtractFromStored(initialAappliedTransaction.getAffectedStored().getFirst())
								  .amount(Quantities.getQuantity(3, item.getUnit()))
								  .build();

		this.clearQueues();

		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(2, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		AmountStored addedTo;

		try{
			addedTo = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, null, appliedTransaction.getAffectedStored().getFirst());
		} catch(DbDeletedException e) {
			addedTo = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, null, appliedTransaction.getAffectedStored().getLast());
		}

		assertEquals(2, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(3, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(1, storedSearchResult.getNumResults());
		storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.STORED));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = addedTo;
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(2, item.getUnit()), stored.getAmount());
		assertTrue(stored.isState(StoredStateType.STORED));
		StoredInBlock storedInBlock = (StoredInBlock) stored.getState();
		assertEquals(item.getStorageBlockIds().findFirst().get(), storedInBlock.getStorageBlock());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		ObjectHistoryEvent event = storedHistory.getResults().stream().filter(e->e.getDetails() != null && e.getDetails().containsKey(ITEM_TRANSACTION.name())).findFirst().orElseThrow();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddAmountInTransitSuccessBulkPart() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlockIds().findFirst().get())
														.build();
		AppliedTransaction initialAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		preApplyTransaction = StoredToInTransitAmountTransaction.builder()
								  .subtractFromStored(initialAappliedTransaction.getAffectedStored().getFirst())
								  .amount(Quantities.getQuantity(3, item.getUnit()))
								  .build();

		this.clearQueues();

		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(2, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		AmountStored addedTo;

		try{
			addedTo = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, null, appliedTransaction.getAffectedStored().getFirst());
		} catch(DbDeletedException e) {
			addedTo = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, null, appliedTransaction.getAffectedStored().getLast());
		}

		assertEquals(2, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(3, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(1, storedSearchResult.getNumResults());
		storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.STORED));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = addedTo;
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(2, item.getUnit()), stored.getAmount());
		assertTrue(stored.isState(StoredStateType.STORED));
		StoredInBlock storedInBlock = (StoredInBlock) stored.getState();
		assertEquals(item.getStorageBlockIds().findFirst().get(), storedInBlock.getStorageBlock());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		ObjectHistoryEvent event = storedHistory.getResults().stream().filter(e->e.getDetails() != null && e.getDetails().containsKey(ITEM_TRANSACTION.name())).findFirst().orElseThrow();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}


	@Test
	public void applyAddAmountInTransitSuccessAmtListAllFlag() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlockIds().findFirst().get())
														.build();
		AppliedTransaction initialAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		preApplyTransaction = StoredToInTransitAmountTransaction.builder()
								  .subtractFromStored(initialAappliedTransaction.getAffectedStored().getFirst())
								  .all(true)
								  .build();

		this.clearQueues();

		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(2, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		AmountStored addedTo;

		try{
			addedTo = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, null, appliedTransaction.getAffectedStored().getFirst());
		} catch(DbDeletedException e) {
			addedTo = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, null, appliedTransaction.getAffectedStored().getLast());
		}

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.STORED));
		assertEquals(0, storedSearchResult.getNumResults());

		AmountStored stored = addedTo;
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
		assertTrue(stored.isState(StoredStateType.IN_TRANSIT));

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		ObjectHistoryEvent event = storedHistory.getResults().stream().filter(e->e.getDetails() != null && e.getDetails().containsKey(ITEM_TRANSACTION.name())).findFirst().orElseThrow();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}




}
