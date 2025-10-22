package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;

import jakarta.inject.Inject;
import tech.units.indriya.unit.Units;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
class InventoryItemServiceTest extends MongoHistoriedServiceTest<InventoryItem, InventoryItemService> {

	@Inject
	InventoryItemService inventoryItemService;

	@Inject
	InventoryItemTestObjectCreator itemTestObjectCreator;

	@Override
	protected InventoryItem getTestObject() {
		return itemTestObjectCreator.getTestObject();
	}

	@Test
	public void injectTest() {
		assertNotNull(inventoryItemService);
	}

	@Test
	public void listTest() {
		this.defaultListTest(this.inventoryItemService);
	}

	@Test
	public void countTest() {
		this.defaultCountTest(this.inventoryItemService);
	}

	@Test
	public void addTest() {
		this.defaultAddTest(this.inventoryItemService);
	}

	@Test
	public void getObjectIdTest() {
		this.defaultGetObjectIdTest(this.inventoryItemService);
	}

	@Test
	public void getStringTest() {
		this.defaultGetStringTest(this.inventoryItemService);
	}

	@Test
	public void removeAllTest() {
		this.defaultRemoveAllTest(this.inventoryItemService);
	}

	@Test
	public void updatePassTest() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put("name", FAKER.name().name());
		updates.put("description", FAKER.lorem().paragraph());
		updates.put("storageType", item.getStorageType().name());
		//TODO:: finish; storage blocks, files?, images?

		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user);
		item = this.inventoryItemService.get(DEFAULT_TEST_DB_NAME, newId);

		assertEquals(updates.get("name").asText(), item.getName());
		assertEquals(updates.get("description").asText(), item.getDescription());
		assertEquals(updates.get("storageType").asText(), item.getStorageType().name());
	}

	@Test
	public void testInvalidNameUpdateNull() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put("name", (String) null);

		Exception exception = assertThrows(IllegalArgumentException.class, () -> this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user));
		log.info("Exception: {}", exception.getMessage());
	}

	@Test
	public void testInvalidUniqueUnit() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject().setStorageType(StorageType.UNIQUE_SINGLE).setUnit(Units.GRAM);

		Exception exception = assertThrows(ConstraintViolationException.class, () -> this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user));
		log.info("Exception: {}", exception.getMessage());
	}

	@Test
	public void testInvalidNameUpdateBlank() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		InventoryItem other = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);
		ObjectId otherId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, other, user);

		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put("name", "");

		Exception exception = assertThrows(IllegalArgumentException.class, () -> this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user));
		log.info("Exception: {}", exception.getMessage());
	}

	@Test
	public void testInvalidNameCreateEqualsAnother() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem other = this.getTestObject();
		InventoryItem item = this.getTestObject().setName(other.getName());
		ObjectId otherId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, other, user);

		Exception exception = assertThrows(ValidationException.class, () -> this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user));
		log.info("Exception: {}", exception.getMessage());
	}

	@Test
	public void testInvalidNameUpdateEqualsAnother() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		InventoryItem other = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);
		ObjectId otherId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, other, user);

		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put("name", other.getName());

		Exception exception = assertThrows(ValidationException.class, () -> this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user));
		log.info("Exception: {}", exception.getMessage());
	}

	@Test
	public void testInvalidUpdateStorageType() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put("storageType", StorageType.AMOUNT_LIST.name());

		Exception exception = assertThrows(IllegalArgumentException.class, () -> this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user));
		log.info("Exception: {}", exception.getMessage());
	}

	@Test
	public void testUpdateUnit() throws JsonProcessingException {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put(
			"unit",
			ObjectUtils.OBJECT_MAPPER.readTree("{\"string\":\"mol\"}")
		);

		item = this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user);
		assertEquals(Units.MOLE, item.getUnit());
	}

	@Test
	public void testInvalidUpdateUnit() throws JsonProcessingException {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);

		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put(
			"unit",
			ObjectUtils.OBJECT_MAPPER.readTree("{\"string\":\"gal\"}")
		);

		Exception exception = assertThrows(ValidationException.class, () -> this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user));
		log.info("Exception: {}", exception.getMessage());
	}

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