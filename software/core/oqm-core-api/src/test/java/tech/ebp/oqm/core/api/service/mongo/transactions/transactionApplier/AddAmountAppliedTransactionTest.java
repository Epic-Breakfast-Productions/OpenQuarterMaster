package tech.ebp.oqm.core.api.service.mongo.transactions.transactionApplier;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
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
public class AddAmountAppliedTransactionTest extends AppliedTransactionServiceTest {
	
	@Test
	public void applyAddAmountSuccessBulkNotInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		
		
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlocks().getFirst())
														.build();
		
		try {
			this.clearQueues();
		} catch(UnknownTopicOrPartitionException e) {
			log.warn("Failed to clear queues", e);
		}
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		//TODO:: this, #1080
		//		List<EventNotificationWrapper> messages = this.assertMessages(
		//			EventType.CREATE,
		//			EventType.UPDATE
		//		);
		//TODO:: verify messages
	}
	
	@Test
	public void applyAddAmountSuccessBulkAlreadyInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		
		AmountStored originalStored = AmountStored.builder()
										  .item(item.getId())
										  .state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst()).build())
										  .amount(Quantities.getQuantity(5, item.getUnit()))
										  .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);
		
		
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlocks().getFirst())
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(10, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(originalStored.getId(), stored.getId());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(10, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}
	
	@Test
	public void applyAddAmountSuccessAmtListNewEntry() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlocks().getFirst())
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}
	
	@Test
	public void applyAddAmountSuccessAmtListExistingStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		
		AmountStored originalStored = AmountStored.builder()
										  .item(item.getId())
										  .state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst()).build())
										  .amount(Quantities.getQuantity(5, item.getUnit()))
										  .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);
		
		
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlocks().getFirst())
														.toStored(originalStored.getId())
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(10, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(originalStored.getId(), stored.getId());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(10, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}
	
	@Test
	public void applyAddAmountFailUniqueMulti() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);
		
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlocks().getFirst())
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add an amount to a unique item.", e.getMessage());
	}
	
	@Test
	public void applyAddAmountFailUniqueSingle() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);
		
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(item.getStorageBlocks().getFirst())
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add an amount to a unique item.", e.getMessage());
	}
	
	@Test
	public void applyAddAmountFailBulkBlockNotInInventory() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		
		ObjectId badBlockId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toBlock(badBlockId)
														.build();
		
		ValidationException e = assertThrows(ValidationException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Storage block " + badBlockId + " not used to hold this item (" + item.getId().toHexString() + ").", e.getMessage());
	}
	
	@Test
	public void applyAddAmountFailBulkStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		
		ObjectId badStoredId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toStored(badStoredId)
														.toBlock(item.getStorageBlocks().getFirst())
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored given does not match stored found in block.", e.getMessage());
	}
	
	@Test
	public void applyAddAmountFailAmtListBadStored() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		
		ObjectId badStoredId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toStored(badStoredId)
														.toBlock(item.getStorageBlocks().getFirst())
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Could not find Stored with id " + badStoredId, e.getMessage());
	}
	
	@Test
	public void applyAddAmountFailAmtListBadStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		
		ObjectId origBlock = item.getStorageBlocks().getFirst();
		ObjectId otherBlockId = this.storageBlockService.add(DEFAULT_TEST_DB_NAME, StorageBlock.builder().label(FAKER.location().building()).build(), entity).getId();
		
		item.getStorageBlocks().add(otherBlockId);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);
		
		AmountStored originalStored = AmountStored.builder()
										  .item(item.getId())
										  .state(StoredInBlock.builder().storageBlock(origBlock).build())
										  .amount(Quantities.getQuantity(5, item.getUnit()))
										  .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);
		
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.toStored(originalStored.getId())
														.toBlock(otherBlockId)
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored given does not exist in block.", e.getMessage());
	}
	
	//TODO:: any more?
}
