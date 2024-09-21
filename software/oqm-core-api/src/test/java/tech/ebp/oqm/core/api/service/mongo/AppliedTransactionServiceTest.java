
package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.history.events.CreateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set.SetAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.AppliedTransactionSearch;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.StorageBlockTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoObjectServiceTest;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.model.object.history.details.HistoryDetailType.ITEM_TRANSACTION;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class AppliedTransactionServiceTest extends MongoObjectServiceTest<AppliedTransaction, AppliedTransactionService> {

	@Inject
	AppliedTransactionService appliedTransactionService;

	@Inject
	StorageBlockService storageBlockService;

	@Inject
	StorageBlockTestObjectCreator storageBlockTestObjectCreator;

	@Inject
	InventoryItemService inventoryItemService;

	@Inject
	InventoryItemTestObjectCreator itemTestObjectCreator;

	@Inject
	StoredService storedService;

	//TODO:: these default tests
	@Override
	protected AppliedTransaction getTestObject() {
		return null;//TODO
	}
//
//	@Test
//	public void injectTest() {
//		assertNotNull(appliedTransactionService);
//	}
//
//	@Test
//	public void listTest() {
//		this.defaultListTest(this.appliedTransactionService);
//	}
//
//	@Test
//	public void countTest() {
//		this.defaultCountTest(this.appliedTransactionService);
//	}
//
//	@Test
//	public void addTest() {
//		this.defaultAddTest(this.appliedTransactionService);
//	}
//
//	@Test
//	public void getObjectIdTest() {
//		this.defaultGetObjectIdTest(this.appliedTransactionService);
//	}
//
//	@Test
//	public void getStringTest() {
//		this.defaultGetStringTest(this.appliedTransactionService);
//	}
//
//	@Test
//	public void removeAllTest() {
//		this.defaultRemoveAllTest(this.appliedTransactionService);
//	}


	private InventoryItem setupItem(StorageType storageType, InteractingEntity entity) {
		StorageBlock storageBlock = storageBlockTestObjectCreator.getTestObject();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, storageBlock, entity);
		InventoryItem item = this.itemTestObjectCreator.getTestObject().setStorageType(storageType);
		item.getStorageBlocks().add(storageBlock.getId());
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, entity);
		return item;
	}


	//<editor-fold desc="Apply- Add Amount">
	@Test
	public void applyAddAmountSuccessBulkNotInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
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
	public void applyAddAmountSuccessBulkAlreadyInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		AmountStored originalStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);


		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(10, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
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

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
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
			.storageBlock(item.getStorageBlocks().getFirst())
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);


		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toBlock(item.getStorageBlocks().getFirst())
			.toStored(originalStored.getId())
			.build();

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(10, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		ValidationException e = assertThrows(ValidationException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Storage block " + badBlockId + " not used to hold this item.", e.getMessage());
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Could not find Stored with id " + badStoredId, e.getMessage());
	}

	@Test
	public void applyAddAmountFailAmtListBadStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ObjectId origBlock = item.getStorageBlocks().getFirst();
		ObjectId otherBlockId = this.storageBlockService.add(DEFAULT_TEST_DB_NAME, StorageBlock.builder().label(FAKER.location().building()).build(), entity);

		item.getStorageBlocks().add(otherBlockId);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		AmountStored originalStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(origBlock)
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toStored(originalStored.getId())
			.toBlock(otherBlockId)
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored given does not exist in block.", e.getMessage());
	}

	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Add Whole">

	@Test
	public void applyAddWholeSuccessAmtList() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
			.toAdd(
				AmountStored.builder()
					.item(item.getId())
					.amount(Quantities.getQuantity(5, item.getUnit()))
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		CreateEvent event = (CreateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddWholeSuccessUniqueMulti() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
			.toAdd(
				UniqueStored.builder()
					.item(item.getId())
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(1, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		UniqueStored storedFromSearch = (UniqueStored) storedSearchResult.getResults().getFirst();

		UniqueStored stored = (UniqueStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		CreateEvent event = (CreateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddWholeSuccessUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
			.toAdd(
				UniqueStored.builder()
					.item(item.getId())
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(1, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		UniqueStored storedFromSearch = (UniqueStored) storedSearchResult.getResults().getFirst();

		UniqueStored stored = (UniqueStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		CreateEvent event = (CreateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddWholeFailBulk() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
			.toAdd(
				AmountStored.builder()
					.item(item.getId())
					.amount(Quantities.getQuantity(5, item.getUnit()))
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add whole item to a bulk storage typed item.", e.getMessage());
	}

	@Test
	public void applyAddWholeFailAnotherUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
			.toAdd(
				UniqueStored.builder()
					.item(item.getId())
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		ValidationException e = assertThrows(ValidationException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add more than one stored held for type UNIQUE_SINGLE", e.getMessage());
	}

	@Test
	public void applyAddWholeFailMismatchedBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
			.toAdd(
				UniqueStored.builder()
					.item(item.getId())
					.storageBlock(new ObjectId())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Block given does not match block marked in stored.", e.getMessage());

		ItemStoredTransaction preApplyTransaction2 = AddWholeTransaction.builder()
			.toAdd(
				UniqueStored.builder()
					.item(item.getId())
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(new ObjectId())
			.build();

		e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction2, entity));
		assertEquals("To Block given does not match block marked in stored.", e.getMessage());
	}

	@Test
	public void applyAddWholeFailBlockNotInItem() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
			.toAdd(
				UniqueStored.builder()
					.item(item.getId())
					.storageBlock(new ObjectId())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Block given does not match block marked in stored.", e.getMessage());
	}

	@Test
	public void applyAddWholeFailItemMismatched() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
			.toAdd(
				UniqueStored.builder()
					.item(new ObjectId())
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored given is not associated with item.", e.getMessage());
	}

	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Checkin Full">
	//TODO:: Success - amount - to stored (existing)
	//TODO:: Success - amount - to stored (not existing)
	//TODO:: Success - amount - to block
	//TODO:: Fail - amount - No 'to' given
	//TODO:: Fail - amount - 'to block' given for list
	//TODO:: Success - whole - to block
	//TODO:: fail - whole - no to block given
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Checkin Part">
	//TODO:: When we have this implemented
//</editor-fold>
//<editor-fold desc="Apply- Checkout Amount">
	//TODO:: Success - bulk - from block
	//TODO:: Success - bulk - from stored
	//TODO:: fail - bulk - no 'from' given
	//TODO:: Success - amt list - from stored
	//TODO:: fail - amt list - from block/ not from stored
	//TODO:: fail - Unique given
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Checkout Whole">
	//TODO:: Success - bulk
	//TODO:: Success - amt list
	//TODO:: Success - unique list
	//TODO:: Success - unique single
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Set Amount">
	@Test
	public void applySetAmountSuccessBulkNotInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
			.build();

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
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
	public void applySetAmountSuccessBulkAlreadyInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		AmountStored originalStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);


		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(6, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
			.build();

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(6, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(originalStored.getId(), stored.getId());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(6, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}


	@Test
	public void applySetAmountSuccessAmtListExistingStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		AmountStored originalStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);


		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(6, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
			.stored(originalStored.getId())
			.build();

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(6, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
		//TODO:: storage block stats

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(originalStored.getId(), stored.getId());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(6, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applySetAmountFailUniqueMulti() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add an amount to a unique item.", e.getMessage());
	}

	@Test
	public void applySetAmountFailUniqueSingle() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add an amount to a unique item.", e.getMessage());
	}

	@Test
	public void applySetAmountFailBulkBlockNotInInventory() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ObjectId badBlockId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.block(badBlockId)
			.build();

		ValidationException e = assertThrows(ValidationException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Storage block " + badBlockId + " not used to hold this item.", e.getMessage());
	}

	@Test
	public void applySetAmountFailBulkStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ObjectId badStoredId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.stored(badStoredId)
			.block(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored given does not match stored found in block.", e.getMessage());
	}

	@Test
	public void applySetAmountFailAmtListBadStored() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ObjectId badStoredId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.stored(badStoredId)
			.block(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Could not find Stored with id " + badStoredId, e.getMessage());
	}

	@Test
	public void applySetAmountFailAmtListBadStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ObjectId origBlock = item.getStorageBlocks().getFirst();
		ObjectId otherBlockId = this.storageBlockService.add(DEFAULT_TEST_DB_NAME, StorageBlock.builder().label(FAKER.location().building()).build(), entity);

		item.getStorageBlocks().add(otherBlockId);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		AmountStored originalStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(origBlock)
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.stored(originalStored.getId())
			.block(otherBlockId)
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored given does not exist in block.", e.getMessage());
	}

	@Test
	public void applySetAmountFailAmtListNewEntry() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Must specify a stored to set the amount of.", e.getMessage());
	}

	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Subtract Amount">
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

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
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

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
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

		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getStatsAfterApply().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getStatsAfterApply().getTotal());
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
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Could not find Stored with id null", e.getMessage());
	}

	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Subtract Whole">
	//TODO:: Success - bulk
	//TODO:: Success - amt list
	//TODO:: Success - unique list
	//TODO:: Success - unique single
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Transfer Amount">
	//TODO:: Success - bulk - to existing
	//TODO:: Success - bulk - to not existing
	//TODO:: Success - amt list - to stored given
	//TODO:: Success - amt list - no to stored given
	//TODO:: fail - unique anything
	//TODO:: fail - less than 0 in from stored
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Transfer Whole">
	//TODO:: Success - bulk
	//TODO:: Success - amt list
	//TODO:: Success - unique list
	//TODO:: Success - unique single
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
}