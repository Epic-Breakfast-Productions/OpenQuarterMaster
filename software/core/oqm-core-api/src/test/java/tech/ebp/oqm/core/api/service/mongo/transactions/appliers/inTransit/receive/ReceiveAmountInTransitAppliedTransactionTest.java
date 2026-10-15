package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.receive;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.exception.db.DbDeletedException;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.GenericLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveAmountInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveWholeInTransitTransaction;
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
public class ReceiveAmountInTransitAppliedTransactionTest extends InTransitAppliedTransactionTest {

	//<editor-fold desc="BULK items">
	@Test
	public void applyAddAmountInTransitSuccessBulkAddAllFlagToBlockStoredNoExist() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddWholeInTransitTransaction.builder()
														.toAdd(AmountStored.builder()
																   .item(item.getId())
																   .amount(Quantities.getQuantity(5, item.getUnit()))
																   .build())
														.details(inTransitDetails)
														.build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toBlock(item.getStorageBlockIds().findFirst().get())
								  .all(true)
								  .build();

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

		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(0, storedSearchResult.getNumResults());
		storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.STORED));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = addedTo;
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
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
	public void applyAddAmountInTransitSuccessBulkAddAllQuantityToBlockStoredNoExist() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddWholeInTransitTransaction.builder()
														.toAdd(AmountStored.builder()
																   .item(item.getId())
																   .amount(Quantities.getQuantity(5, item.getUnit()))
																   .build())
														.details(inTransitDetails)
														.build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toBlock(item.getStorageBlockIds().findFirst().get())
								  .amount(((AmountStored)((AddWholeInTransitTransaction)preApplyTransaction).getToAdd()).getAmount())
								  .all(false)
								  .build();

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

		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(0, storedSearchResult.getNumResults());
		storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.STORED));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = addedTo;
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
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
	public void applyAddAmountInTransitSuccessBulkAddAllFlagToBlockStoredExist() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlockIds().findFirst().get())
														.build();
		AppliedTransaction initialAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		preApplyTransaction = AddWholeInTransitTransaction.builder()
								  .toAdd(AmountStored.builder()
											 .item(item.getId())
											 .amount(Quantities.getQuantity(5, item.getUnit()))
											 .build())
								  .details(inTransitDetails)
								  .build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toBlock(item.getStorageBlockIds().findFirst().get())
								  .all(true)
								  .build();

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
		assertEquals(Quantities.getQuantity(10, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());

		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(0, storedSearchResult.getNumResults());
		storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.STORED));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = addedTo;
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(10, item.getUnit()), stored.getAmount());
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
	public void applyAddAmountInTransitSuccessBulkAddAllQuantityToBlockNoExist() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddWholeInTransitTransaction.builder()
														.toAdd(AmountStored.builder()
																   .item(item.getId())
																   .amount(Quantities.getQuantity(5, item.getUnit()))
																   .build())
														.details(inTransitDetails)
														.build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toBlock(ObjectId.get())
								  .amount(((AmountStored)((AddWholeInTransitTransaction)preApplyTransaction).getToAdd()).getAmount())
								  .all(false)
								  .build();

		ItemStoredTransaction finalPreApplyTransaction = preApplyTransaction;
		assertThrows(
			ValidationException.class,
			()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, finalPreApplyTransaction, entity)
		);
	}








	@Test
	public void applyAddAmountInTransitSuccessBulkAddAllQuantityToStoredNoExist() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddWholeInTransitTransaction.builder()
														.toAdd(AmountStored.builder()
																   .item(item.getId())
																   .amount(Quantities.getQuantity(5, item.getUnit()))
																   .build())
														.details(inTransitDetails)
														.build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toStored(ObjectId.get())
								  .amount(((AmountStored)((AddWholeInTransitTransaction)preApplyTransaction).getToAdd()).getAmount())
								  .all(false)
								  .build();

		ItemStoredTransaction finalPreApplyTransaction = preApplyTransaction;
		assertThrows(
			DbNotFoundException.class,
			()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, finalPreApplyTransaction, entity)
		);

	}


	@Test
	public void applyAddAmountInTransitSuccessBulkAddAllFlagToStoredExist() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlockIds().findFirst().get())
														.build();
		AppliedTransaction initialAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		preApplyTransaction = AddWholeInTransitTransaction.builder()
								  .toAdd(AmountStored.builder()
											 .item(item.getId())
											 .amount(Quantities.getQuantity(5, item.getUnit()))
											 .build())
								  .details(inTransitDetails)
								  .build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toStored(initialAappliedTransaction.getAffectedStored().getFirst())
								  .all(true)
								  .build();

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
		assertEquals(Quantities.getQuantity(10, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());

		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(0, storedSearchResult.getNumResults());
		storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.STORED));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = addedTo;
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(10, item.getUnit()), stored.getAmount());
		assertTrue(stored.isState(StoredStateType.STORED));
		StoredInBlock storedInBlock = (StoredInBlock) stored.getState();
		assertEquals(item.getStorageBlockIds().findFirst().get(), storedInBlock.getStorageBlock());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		ObjectHistoryEvent event = storedHistory.getResults().stream().filter(e->e.getDetails() != null && e.getDetails().containsKey(ITEM_TRANSACTION.name())).findFirst().orElseThrow();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	//</editor-fold>

	//TODO:: bulk, add part


	//<editor-fold desc="AMOUNT_LIST items">
	@Test
	public void applyAddAmountInTransitSuccessAmountListAddAllFlagToBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddWholeInTransitTransaction.builder()
														.toAdd(AmountStored.builder()
																   .item(item.getId())
																   .amount(Quantities.getQuantity(5, item.getUnit()))
																   .build())
														.details(inTransitDetails)
														.build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toBlock(item.getStorageBlockIds().findFirst().get())
								  .all(true)
								  .build();

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

		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(0, storedSearchResult.getNumResults());
		storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.STORED));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = addedTo;
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
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
	public void applyAddAmountInTransitSuccessAmountListAddAllQuantityToBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddWholeInTransitTransaction.builder()
														.toAdd(AmountStored.builder()
																   .item(item.getId())
																   .amount(Quantities.getQuantity(5, item.getUnit()))
																   .build())
														.details(inTransitDetails)
														.build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toBlock(item.getStorageBlockIds().findFirst().get())
								  .amount(((AmountStored)((AddWholeInTransitTransaction)preApplyTransaction).getToAdd()).getAmount())
								  .all(false)
								  .build();

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

		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(0, storedSearchResult.getNumResults());
		storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.STORED));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = addedTo;
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
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
	public void applyAddAmountInTransitSuccessAmountListAddAllQuantityToBlockNoExist() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddWholeInTransitTransaction.builder()
														.toAdd(AmountStored.builder()
																   .item(item.getId())
																   .amount(Quantities.getQuantity(5, item.getUnit()))
																   .build())
														.details(inTransitDetails)
														.build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toBlock(ObjectId.get())
								  .amount(((AmountStored)((AddWholeInTransitTransaction)preApplyTransaction).getToAdd()).getAmount())
								  .all(false)
								  .build();

		ItemStoredTransaction finalPreApplyTransaction = preApplyTransaction;
		assertThrows(
			ValidationException.class,
			()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, finalPreApplyTransaction, entity)
		);
	}

	@Test
	public void applyAddAmountInTransitSuccessAmountListAddAllQuantityToStoredNoExist() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddWholeInTransitTransaction.builder()
														.toAdd(AmountStored.builder()
																   .item(item.getId())
																   .amount(Quantities.getQuantity(5, item.getUnit()))
																   .build())
														.details(inTransitDetails)
														.build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toStored(ObjectId.get())
								  .amount(((AmountStored)((AddWholeInTransitTransaction)preApplyTransaction).getToAdd()).getAmount())
								  .all(false)
								  .build();

		ItemStoredTransaction finalPreApplyTransaction = preApplyTransaction;
		assertThrows(
			DbNotFoundException.class,
			()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, finalPreApplyTransaction, entity)
		);

	}


	@Test
	public void applyAddAmountInTransitSuccessAmountListAddAllFlagToStoredExist() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		InTransit inTransitDetails = InTransit.builder()
										 .from(GenericLocale.builder().name("Origin").build())
										 .build();

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlockIds().findFirst().get())
														.build();
		AppliedTransaction initialAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		preApplyTransaction = AddWholeInTransitTransaction.builder()
								  .toAdd(AmountStored.builder()
											 .item(item.getId())
											 .amount(Quantities.getQuantity(5, item.getUnit()))
											 .build())
								  .details(inTransitDetails)
								  .build();

		AppliedTransaction firstAappliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		this.clearQueues();

		preApplyTransaction = ReceiveAmountInTransitTransaction.builder()
								  .inTransitStored(firstAappliedTransaction.getAffectedStored().getFirst())
								  .toStored(initialAappliedTransaction.getAffectedStored().getFirst())
								  .all(true)
								  .build();

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
		assertEquals(Quantities.getQuantity(10, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());

		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(0, storedSearchResult.getNumResults());
		storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.STORED));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = addedTo;
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(10, item.getUnit()), stored.getAmount());
		assertTrue(stored.isState(StoredStateType.STORED));
		StoredInBlock storedInBlock = (StoredInBlock) stored.getState();
		assertEquals(item.getStorageBlockIds().findFirst().get(), storedInBlock.getStorageBlock());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		ObjectHistoryEvent event = storedHistory.getResults().stream().filter(e->e.getDetails() != null && e.getDetails().containsKey(ITEM_TRANSACTION.name())).findFirst().orElseThrow();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	//TODO:: part

	//</editor-fold>

	//TODO:: fail on Unique
}
