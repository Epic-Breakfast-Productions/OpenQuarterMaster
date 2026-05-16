package tech.ebp.oqm.core.api.service.mongo.transactions.transactionApplier;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
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
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set.SetAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
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
public class SubtractAmountAppliedTransactionTest extends AppliedTransactionServiceTest {
	@Test
	public void applySubtractAmountSuccessBulkBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.item(item.getId())
				.storageBlock(item.getStorageBlocks().getFirst())
				.build(),
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(item.getStorageBlocks().getFirst())
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(0, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}
	
	@Test
	public void applySubtractAmountSuccessBulkStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		
		AmountStored initialStored = AmountStored.builder()
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(item.getStorageBlocks().getFirst())
														.fromStored(initialStored.getId())
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(0, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}
	
	@Test
	public void applySubtractAmountSuccessList() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		
		AmountStored initialStored = AmountStored.builder()
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(item.getStorageBlocks().getFirst())
														.fromStored(initialStored.getId())
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(0, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}
	
	@Test
	public void applySubtractAmountFailUniqueMulti() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);
		
		UniqueStored initialStored = UniqueStored.builder()
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(item.getStorageBlocks().getFirst())
														.fromStored(initialStored.getId())
														.build();
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot subtract an amount from a unique item.", e.getMessage());
	}
	
	@Test
	public void applySubtractAmountFailUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);
		
		UniqueStored initialStored = UniqueStored.builder()
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(item.getStorageBlocks().getFirst())
														.fromStored(initialStored.getId())
														.build();
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot subtract an amount from a unique item.", e.getMessage());
	}
	
	@Test
	public void applySubtractAmountFailBulkSubMoreThanHeld() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		
		AmountStored initialStored = AmountStored.builder()
										 .amount(Quantities.getQuantity(2, item.getUnit()))
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(item.getStorageBlocks().getFirst())
														.fromStored(initialStored.getId())
														.build();
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Resulting amount less than zero. (subtracting 5 units from 2 units resulting in -3 units)", e.getMessage());
	}
	
	@Test
	public void applySubtractAmountFailAmtListSubMoreThanHeld() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		
		AmountStored initialStored = AmountStored.builder()
										 .amount(Quantities.getQuantity(2, item.getUnit()))
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(item.getStorageBlocks().getFirst())
														.fromStored(initialStored.getId())
														.build();
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Resulting amount less than zero. (subtracting 5 units from 2 units resulting in -3 units)", e.getMessage());
	}
	
	@Test
	public void applySubtractAmountFailBulkMismatchedBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		
		StorageBlock newBlock = StorageBlock.builder().label(FAKER.location().building()).build();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, newBlock, entity);
		item.getStorageBlocks().add(newBlock.getId());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);
		
		AmountStored initialStored = AmountStored.builder()
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		AmountStored secondStored = AmountStored.builder()
										.amount(Quantities.getQuantity(5, item.getUnit()))
										.item(item.getId())
										.storageBlock(newBlock.getId())
										.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			secondStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(newBlock.getId())
														.fromStored(initialStored.getId())
														.build();
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored id in transaction not the id of stored found.", e.getMessage());
	}
	
	@Test
	public void applySubtractAmountFailBulkMismatchedStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		
		StorageBlock newBlock = StorageBlock.builder().label(FAKER.location().building()).build();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, newBlock, entity);
		item.getStorageBlocks().add(newBlock.getId());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);
		
		AmountStored initialStored = AmountStored.builder()
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		AmountStored secondStored = AmountStored.builder()
										.amount(Quantities.getQuantity(5, item.getUnit()))
										.item(item.getId())
										.storageBlock(newBlock.getId())
										.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			secondStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(item.getStorageBlocks().getFirst())
														.fromStored(secondStored.getId())
														.build();
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored id in transaction not the id of stored found.", e.getMessage());
	}
	
	@Test
	public void applySubtractAmountFailListMismatchedBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		
		StorageBlock newBlock = StorageBlock.builder().label(FAKER.location().building()).build();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, newBlock, entity);
		item.getStorageBlocks().add(newBlock.getId());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);
		
		AmountStored initialStored = AmountStored.builder()
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		AmountStored secondStored = AmountStored.builder()
										.amount(Quantities.getQuantity(5, item.getUnit()))
										.item(item.getId())
										.storageBlock(newBlock.getId())
										.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			secondStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(newBlock.getId())
														.fromStored(initialStored.getId())
														.build();
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored retrieved not in specified block.", e.getMessage());
	}
	
	@Test
	public void applySubtractAmountFailListMismatchedStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		
		StorageBlock newBlock = StorageBlock.builder().label(FAKER.location().building()).build();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, newBlock, entity);
		item.getStorageBlocks().add(newBlock.getId());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);
		
		AmountStored initialStored = AmountStored.builder()
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		AmountStored secondStored = AmountStored.builder()
										.amount(Quantities.getQuantity(5, item.getUnit()))
										.item(item.getId())
										.storageBlock(newBlock.getId())
										.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			secondStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(item.getStorageBlocks().getFirst())
														.fromStored(secondStored.getId())
														.build();
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored retrieved not in specified block.", e.getMessage());
	}
	
	@Test
	public void applySubtractAmountFailNullStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		
		AmountStored initialStored = AmountStored.builder()
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .item(item.getId())
										 .storageBlock(item.getStorageBlocks().getFirst())
										 .build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		
		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(item.getStorageBlocks().getFirst())
														.build();
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Must specify the stored item we are subtracting from.", e.getMessage());
	}
	
	//TODO:: any more?
}
