package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.inTransit.add;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.GenericLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StoredLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddAmountInTransitTransaction;
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
public class AddAmountInTransitAppliedTransactionTest extends InTransitAppliedTransactionTest {

	@Test
	public void applyAddAmountInTransitSuccessBulk() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		InTransit inTransitDetails = InTransit.builder()
											 .from(GenericLocale.builder().name("Origin").build())
											 .build();

		ItemStoredTransaction preApplyTransaction = AddAmountInTransitTransaction.builder()
																	.amount(Quantities.getQuantity(5, item.getUnit()))
																	.details(inTransitDetails)
																	.build();

		this.clearQueues();

		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));


		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getInTransitStoredStats().getTotal());

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals(1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
		assertTrue(stored.isState(StoredStateType.IN_TRANSIT));
		InTransit storedInTransit = (InTransit) stored.getState();
		assertEquals(inTransitDetails.getFrom(), storedInTransit.getFrom());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		ObjectHistoryEvent event = storedHistory.getResults().stream().filter(e->e.getDetails() != null && e.getDetails().containsKey(ITEM_TRANSACTION.name())).findFirst().orElseThrow();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddAmountInTransitSuccessAmtList() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		InTransit inTransitDetails = InTransit.builder()
											 .from(GenericLocale.builder().name("Origin").build())
											 .to(GenericLocale.builder().name("Destination").build())
											 .build();

		ItemStoredTransaction preApplyTransaction = AddAmountInTransitTransaction.builder()
																	.amount(Quantities.getQuantity(5, item.getUnit()))
																	.details(inTransitDetails)
																	.build();

		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		//TODO:: stats don't include in transit stored yet
		//		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		//		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()).setStoredState(StoredStateType.IN_TRANSIT));
		assertEquals( 1, storedSearchResult.getNumResults());
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
		assertTrue(stored.isState(StoredStateType.IN_TRANSIT));
		InTransit storedInTransit = (InTransit) stored.getState();
		assertEquals(inTransitDetails.getFrom(), storedInTransit.getFrom());
		assertEquals(inTransitDetails.getTo(), storedInTransit.getTo());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		ObjectHistoryEvent event = storedHistory.getResults().stream().filter(e->e.getDetails() != null && e.getDetails().containsKey(ITEM_TRANSACTION.name())).findFirst().orElseThrow();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddAmountInTransitFailUniqueMulti() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);

		InTransit inTransitDetails = InTransit.builder()
											 .from(GenericLocale.builder().name("Origin").build())
											 .build();

		ItemStoredTransaction preApplyTransaction = AddAmountInTransitTransaction.builder()
																	.amount(Quantities.getQuantity(5, item.getUnit()))
																	.details(inTransitDetails)
																	.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Item is not an amount holding type.", e.getMessage());
	}

	@Test
	public void applyAddAmountInTransitFailUniqueSingle() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		InTransit inTransitDetails = InTransit.builder()
											 .from(GenericLocale.builder().name("Origin").build())
											 .build();

		ItemStoredTransaction preApplyTransaction = AddAmountInTransitTransaction.builder()
																	.amount(Quantities.getQuantity(5, item.getUnit()))
																	.details(inTransitDetails)
																	.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Item is not an amount holding type.", e.getMessage());
	}

	@Test
	public void applyAddAmountInTransitFailStoredLocaleOtherItem() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		InventoryItem otherItem = setupItem(StorageType.BULK, entity);

		AmountStored otherItemStored = AmountStored.builder()
											.item(otherItem.getId())
											.state(StoredInBlock.builder().storageBlock(otherItem.getStorageBlocks().getFirst().getStorageBlock()).build())
											.amount(Quantities.getQuantity(5, otherItem.getUnit()))
											.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, otherItemStored, entity);

		InTransit inTransitDetails = InTransit.builder()
											 .from(StoredLocale.builder().stored(otherItemStored.getId()).build())
											 .build();

		ItemStoredTransaction preApplyTransaction = AddAmountInTransitTransaction.builder()
																	.amount(Quantities.getQuantity(5, item.getUnit()))
																	.details(inTransitDetails)
																	.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored locale must specify stored associated with item in transaction.", e.getMessage());
	}
}
