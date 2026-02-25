package tech.ebp.oqm.core.api.service.mongo;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.StoredTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoHistoriedServiceTest;
import tech.units.indriya.quantity.Quantities;

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
class StoredServiceTest extends MongoHistoriedServiceTest<Stored, StoredService> {

	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	StorageBlockService storageBlockService;

	@Inject
	InventoryItemTestObjectCreator itemTestObjectCreator;
	@Inject
	StoredTestObjectCreator storedTestObjectCreator;

	@Inject
	StoredService storedService;

	@Override
	protected Stored getTestObject() {
		return storedTestObjectCreator.getTestObject();
	}

//	@Test
//	public void injectTest() {
//		assertNotNull(inventoryItemService);
//	}
//
//	@Test
//	public void listTest() {
//		this.defaultListTest(this.inventoryItemService);
//	}
//
//	@Test
//	public void countTest() {
//		this.defaultCountTest(this.inventoryItemService);
//	}
//
//	@Test
//	public void addTest() {
//		this.defaultAddTest(this.inventoryItemService);
//	}
//
//	@Test
//	public void getObjectIdTest() {
//		this.defaultGetObjectIdTest(this.inventoryItemService);
//	}
//
//	@Test
//	public void getStringTest() {
//		this.defaultGetStringTest(this.inventoryItemService);
//	}
//
//	@Test
//	public void removeAllTest() {
//		this.defaultRemoveAllTest(this.inventoryItemService);
//	}

	@Test
	public void addTest() {
		User user = this.getTestUserService().getTestUser();
		ObjectId blockId = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building()),
			user
		).getId();
		InventoryItem item = this.itemTestObjectCreator.getTestObject();
		item.setStorageType(StorageType.BULK).setStorageBlocks(new LinkedHashSet<>(List.of(blockId)));
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		Stored stored = this.storedTestObjectCreator.setItem(item).setStorageBlock(blockId).getTestObject();

		ObjectId storedId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			stored,
			user
		).getId();

		assertNotNull(storedId);
		assertEquals(stored, this.storedService.get(DEFAULT_TEST_DB_NAME, storedId));
	}

	@Test
	public void addInvalidItemTest() {
		User user = this.getTestUserService().getTestUser();
		ObjectId blockId = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building()),
			user
		).getId();
		InventoryItem item = this.itemTestObjectCreator.getTestObject();
		item.setStorageType(StorageType.BULK).setStorageBlocks(new LinkedHashSet<>(List.of(blockId)));
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		Stored stored = this.storedTestObjectCreator.setItem(item).setStorageBlock(blockId).getTestObject();
		stored.setItem(new ObjectId());

		Exception exception = assertThrows(ValidationException.class, () -> this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			stored,
			user
		));
	}

	@Test
	public void addInvalidNonExistentStorageBlockTest() {
		User user = this.getTestUserService().getTestUser();
		ObjectId blockId = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building()),
			user
		).getId();
		InventoryItem item = this.itemTestObjectCreator.getTestObject();
		item.setStorageType(StorageType.BULK).setStorageBlocks(new LinkedHashSet<>(List.of(blockId)));
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		Stored stored = this.storedTestObjectCreator.setItem(item).setStorageBlock(blockId).getTestObject();
		stored.setStorageBlock(new ObjectId());

		Exception exception = assertThrows(ValidationException.class, () -> this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			stored,
			user
		));
	}

	@Test
	public void addInvalidStorageBlockNotInItemTest() {
		User user = this.getTestUserService().getTestUser();
		ObjectId blockId = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building()),
			user
		).getId();
		InventoryItem item = this.itemTestObjectCreator.getTestObject();
		item.setStorageType(StorageType.BULK).setStorageBlocks(new LinkedHashSet<>(List.of(blockId)));
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		Stored stored = this.storedTestObjectCreator.setItem(item).setStorageBlock(blockId).getTestObject();
		stored.setStorageBlock(this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building() + "-2"),
			user
		).getId());

		Exception exception = assertThrows(ValidationException.class, () -> this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			stored,
			user
		));
	}

	@Test
	public void addInvalidTypeTest() {
		User user = this.getTestUserService().getTestUser();
		ObjectId blockId = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building()),
			user
		).getId();
		InventoryItem item = this.itemTestObjectCreator.getTestObject();
		item.setStorageType(StorageType.BULK).setStorageBlocks(new LinkedHashSet<>(List.of(blockId)));
		ObjectId itemId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user).getId();

		Stored stored = UniqueStored.builder().item(itemId).storageBlock(blockId).build();

		Exception exception = assertThrows(ValidationException.class, () -> this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			stored,
			user
		));
	}

	@Test
	public void addInvalidUnitTest() {
		User user = this.getTestUserService().getTestUser();
		ObjectId blockId = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building()),
			user
		).getId();
		InventoryItem item = this.itemTestObjectCreator.getTestObject();
		item.setStorageType(StorageType.BULK).setStorageBlocks(new LinkedHashSet<>(List.of(blockId)));
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		Stored stored = this.storedTestObjectCreator.setItem(item).setStorageBlock(blockId).getTestObject();
		((AmountStored)stored).setAmount(Quantities.getQuantity(0, OqmProvidedUnits.WATT_HOURS));

		Exception exception = assertThrows(ValidationException.class, () -> this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			stored,
			user
		));
	}

	@Test
	public void addInvalidSecondBulkTest() {
		User user = this.getTestUserService().getTestUser();
		ObjectId blockId = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building()),
			user
		).getId();
		InventoryItem item = this.itemTestObjectCreator.getTestObject();
		item.setStorageType(StorageType.BULK).setStorageBlocks(new LinkedHashSet<>(List.of(blockId)));
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		this.storedService.add(DEFAULT_TEST_DB_NAME, this.storedTestObjectCreator.setItem(item).setStorageBlock(blockId).getTestObject(), user);

		Exception exception = assertThrows(ValidationException.class, () -> this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			this.storedTestObjectCreator.setItem(item).setStorageBlock(blockId).getTestObject(),
			user
		));
	}

	@Test
	public void addInvalidSecondUniqueSingleSameBlockTest() {
		User user = this.getTestUserService().getTestUser();
		ObjectId blockId = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building()),
			user
		).getId();
		InventoryItem item = this.itemTestObjectCreator.getTestObject();
		item.setStorageType(StorageType.UNIQUE_SINGLE).setStorageBlocks(new LinkedHashSet<>(List.of(blockId)));
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		this.storedService.add(DEFAULT_TEST_DB_NAME, this.storedTestObjectCreator.setItem(item).setStorageBlock(blockId).getTestObject(), user);

		Exception exception = assertThrows(ValidationException.class, () -> this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			this.storedTestObjectCreator.setItem(item).setStorageBlock(blockId).getTestObject(),
			user
		));
	}

	@Test
	public void addInvalidSecondUniqueSingleDifferentBlockTest() {
		User user = this.getTestUserService().getTestUser();
		ObjectId blockIdOne = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building() + Math.random()),
			user
		).getId();
		ObjectId blockIdTwo = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building() + Math.random()),
			user
		).getId();
		InventoryItem item = this.itemTestObjectCreator.getTestObject();
		item.setStorageType(StorageType.UNIQUE_SINGLE).setStorageBlocks(new LinkedHashSet<>(List.of(blockIdOne)));
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		this.storedService.add(DEFAULT_TEST_DB_NAME, this.storedTestObjectCreator.setItem(item).setStorageBlock(blockIdOne).getTestObject(), user);

		Exception exception = assertThrows(ValidationException.class, () -> this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			this.storedTestObjectCreator.setItem(item).setStorageBlock(blockIdTwo).getTestObject(),
			user
		));
	}


	@Test
	public void searchInStorageBlockTest() {
		User user = this.getTestUserService().getTestUser();
		ObjectId blockId = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building()),
			user
		).getId();
		ObjectId otherBlockId = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			new StorageBlock().setLabel(FAKER.location().building() + "-2"),
			user
		).getId();
		InventoryItem item = this.itemTestObjectCreator.getTestObject();
		item.setStorageType(StorageType.BULK).setStorageBlocks(new LinkedHashSet<>(List.of(blockId, otherBlockId)));
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		Stored stored = this.storedTestObjectCreator.setItem(item).setStorageBlock(blockId).getTestObject();
		Stored otherStored = this.storedTestObjectCreator.setItem(item).setStorageBlock(otherBlockId).getTestObject();

		this.storedService.add(DEFAULT_TEST_DB_NAME, stored, user);
		this.storedService.add(DEFAULT_TEST_DB_NAME, otherStored, user);

		SearchResult<Stored> result = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInStorageBlocks(List.of(blockId)));

		assertEquals(1, result.getNumResults());
		assertEquals(stored, result.getResults().get(0));
	}

	//TODO:: update
	//TODO:: invalid update; amount, item, storage block
	//TODO:: delete

	//TODO:: tests with images, file?


	//TODO:: rework
//	@Test
//	public void testAddSimpleAmountItem(){
//		SimpleAmountItem item = (SimpleAmountItem) new SimpleAmountItem().setName(FAKER.commerce().productName());
//		item.getStorageMap().put(ObjectId.get(), new SingleAmountStoredWrapper(new AmountStored(0, item.getUnit())));
//
//		ObjectId id = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, this.getTestUserService().getTestUser());
//
//		assertNotNull(item.getId());
//		assertEquals(id, item.getId());
//
//		List<InventoryItem> list = inventoryItemService.list(DEFAULT_TEST_DB_NAME);
//		log.info("num in collection: {}", list.size());
//		assertEquals(1, list.size(), "Unexpected number of objects in collection.");
//	}
//
//	@Test
//	public void testAddListAmountItem(){
//		ListAmountItem item = (ListAmountItem) new ListAmountItem().setName(FAKER.commerce().productName());
//		item.add(ObjectId.get(), new AmountStored(0, item.getUnit()));
//
//		ObjectId id = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, this.getTestUserService().getTestUser());
//
//		assertNotNull(item.getId());
//		assertEquals(id, item.getId());
//
//		List<InventoryItem> list = inventoryItemService.list(DEFAULT_TEST_DB_NAME);
//		log.info("num in collection: {}", list.size());
//		assertEquals(1, list.size(), "Unexpected number of objects in collection.");
//	}
//	@Test
//	public void testAddTrackedItem(){
//		TrackedItem item = (TrackedItem) new TrackedItem()
//											 .setTrackedItemIdentifierName("id")
//											 .setName(FAKER.commerce().productName());
//		item.add(ObjectId.get(), new TrackedStored(FAKER.commerce().productName()));
//
//		ObjectId id = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, this.getTestUserService().getTestUser());
//
//		assertNotNull(item.getId());
//		assertEquals(id, item.getId());
//
//		List<InventoryItem> list = inventoryItemService.list(DEFAULT_TEST_DB_NAME);
//		log.info("num in collection: {}", list.size());
//		assertEquals(1, list.size(), "Unexpected number of objects in collection.");
//	}
//
//	@Test
//	public void testUpdateSimpleAmountItemJson(){
//		SimpleAmountItem item = (SimpleAmountItem) new SimpleAmountItem().setName(FAKER.commerce().productName());
//		item.getStorageMap().put(ObjectId.get(), new SingleAmountStoredWrapper(new AmountStored(0, item.getUnit())));
//		ObjectId id = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, this.getTestUserService().getTestUser());
//
//		ObjectNode update = ObjectUtils.OBJECT_MAPPER.valueToTree(item);
//		String newName = item.getName() + " new";
//
//		update.put("name", newName);
//
//		SimpleAmountItem item2 = (SimpleAmountItem) this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, id, update);
//
//		assertEquals(newName, item2.getName());
//	}
//
//	@Test
//	public void testUpdateListAmountItemJson(){
//		ListAmountItem item = (ListAmountItem) new ListAmountItem().setName(FAKER.commerce().productName());
//		item.add(ObjectId.get(), new AmountStored(0, item.getUnit()));
//		ObjectId id = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, this.getTestUserService().getTestUser());
//
//		ObjectNode update = ObjectUtils.OBJECT_MAPPER.valueToTree(item);
//		String newName = item.getName() + " new";
//
//		update.put("name", newName);
//
//		ListAmountItem item2 = (ListAmountItem) this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, id, update);
//
//		assertEquals(newName, item2.getName());
//	}
//
//	@Test
//	public void testUpdateTrackedItemJson(){
//		TrackedItem item = (TrackedItem) new TrackedItem()
//											 .setTrackedItemIdentifierName("id")
//											 .setName(FAKER.commerce().productName());
//		item.add(ObjectId.get(), new TrackedStored(FAKER.commerce().productName()));
//		ObjectId id = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, this.getTestUserService().getTestUser());
//
//		ObjectNode update = ObjectUtils.OBJECT_MAPPER.valueToTree(item);
//		String newName = item.getName() + " new";
//
//		update.put("name", newName);
//
//		TrackedItem item2 = (TrackedItem) this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, id, update);
//
//		assertEquals(newName, item2.getName());
//	}


	//TODO:: rework
//	@Test
//	public void testStoreItemWithNulledStorageBlockKey(){
//		SimpleAmountItem item = (SimpleAmountItem) new SimpleAmountItem().setName(FAKER.commerce().productName());
//		item.getStorageMap().put(new ObjectId(new byte[12]), new SingleAmountStoredWrapper(new AmountStored(0, item.getUnit())));
//		ObjectId id = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, this.getTestUserService().getTestUser());
//
//		SimpleAmountItem returned = (SimpleAmountItem) this.inventoryItemService.get(DEFAULT_TEST_DB_NAME, id);
//		assertEquals(item, returned);
//	}

	//TODO:: test expiry related

	//    @Test
	//    public void listTest(){
	//
	//    }
}