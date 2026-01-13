package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation.Generates;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.GeneratedUniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.ToGenerateUniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation.IdentifierGenerator;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.TotalPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.ItemStoredStats;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;

import jakarta.inject.Inject;
import tech.units.indriya.unit.Units;

import javax.money.Monetary;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class InventoryItemServiceTest extends MongoHistoriedServiceTest<InventoryItem, InventoryItemService> {
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@Inject
	InventoryItemTestObjectCreator itemTestObjectCreator;
	
	@Inject
	IdentifierGenerationService uigs;
	
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
		InventoryItem newItem = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user);
		
		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put("name", FAKER.name().name());
		updates.put("description", FAKER.lorem().paragraph());
		updates.put("storageType", item.getStorageType().name());
		//TODO:: finish; storage blocks, files?, images?
		
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newItem.getId(), updates, user);
		item = this.inventoryItemService.get(DEFAULT_TEST_DB_NAME, newItem.getId());
		
		assertEquals(updates.get("name").asText(), item.getName());
		assertEquals(updates.get("description").asText(), item.getDescription());
		assertEquals(updates.get("storageType").asText(), item.getStorageType().name());
	}
	
	@Test
	public void testInvalidNameUpdateNull() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user).getId();
		
		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put("name", (String) null);
		
		Exception exception = assertThrows(IllegalArgumentException.class, ()->this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user));
		log.info("Exception: {}", exception.getMessage());
	}
	
	@Test
	public void testInvalidUniqueUnit() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject().setStorageType(StorageType.UNIQUE_SINGLE).setUnit(Units.GRAM);
		
		Exception exception = assertThrows(ConstraintViolationException.class, ()->this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user));
		log.info("Exception: {}", exception.getMessage());
	}
	
	@Test
	public void testInvalidNameUpdateBlank() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		InventoryItem other = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user).getId();
		ObjectId otherId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, other, user).getId();
		
		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put("name", "");
		
		Exception exception = assertThrows(IllegalArgumentException.class, ()->this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user));
		log.info("Exception: {}", exception.getMessage());
	}
	
	@Test
	public void testInvalidNameCreateEqualsAnother() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem other = this.getTestObject();
		InventoryItem item = this.getTestObject().setName(other.getName());
		ObjectId otherId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, other, user).getId();
		
		Exception exception = assertThrows(ValidationException.class, ()->this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user));
		log.info("Exception: {}", exception.getMessage());
	}
	
	@Test
	public void testInvalidNameUpdateEqualsAnother() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		InventoryItem other = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user).getId();
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, other, user);
		
		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put("name", other.getName());
		
		Exception exception = assertThrows(ValidationException.class, ()->this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user));
		log.info("Exception: {}", exception.getMessage());
	}
	
	@Test
	public void testInvalidUpdateStorageType() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user).getId();
		
		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put("storageType", StorageType.AMOUNT_LIST.name());
		
		Exception exception = assertThrows(IllegalArgumentException.class, ()->this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user));
		log.info("Exception: {}", exception.getMessage());
	}
	
	@Test
	public void testUpdateUnit() throws JsonProcessingException {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user).getId();
		
		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put(
			"unit",
			ObjectUtils.OBJECT_MAPPER.readTree("{\"string\":\"mol\"}")
		);
		
		item = this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user);
		assertEquals(Units.MOLE, item.getUnit());
		assertEquals(Units.MOLE, item.getStats().getTotal().getUnit());
	}
	
	@Test
	public void testInvalidUpdateUnit() throws JsonProcessingException {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user).getId();
		
		ObjectNode updates = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updates.put(
			"unit",
			ObjectUtils.OBJECT_MAPPER.readTree("{\"string\":\"gal\"}")
		);
		
		Exception exception = assertThrows(ValidationException.class, ()->this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, null, newId, updates, user));
		log.info("Exception: {}", exception.getMessage());
	}
	
	@Test
	public void testGeneratedUniqueIdInAdd() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		
		ObjectId uig = this.uigs.add(DEFAULT_TEST_DB_NAME, IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc}").build(), user).getId();
		
		item.getUniqueIds().add(
			ToGenerateUniqueId.builder().generateFrom(uig)
				.label("SKU").build()
		);
		
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, user).getId();
		
		item = this.inventoryItemService.get(DEFAULT_TEST_DB_NAME, newId);
		
		assertEquals(1, item.getUniqueIds().size());
		
		UniqueId id = item.getUniqueIds().getFirst();
		log.info("generated id: {}", id);
		assertInstanceOf(GeneratedUniqueId.class, id);
	}
	
	@Test
	public void testOtherSameIdDiffGeneratorUniqueIdInAdd() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item1 = this.getTestObject();
		InventoryItem item2 = this.getTestObject();
		
		ObjectId uig1 = this.uigs.add(DEFAULT_TEST_DB_NAME, IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc}").build(), user).getId();
		ObjectId uig2 = this.uigs.add(DEFAULT_TEST_DB_NAME, IdentifierGenerator.builder().name("test2").generates(Generates.UNIQUE).idFormat("{inc}").build(), user).getId();
		
		item1.getUniqueIds().add(
			ToGenerateUniqueId.builder().generateFrom(uig1)
				.label("SKU").build()
		);
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item1, user).getId();
		
		item2.getUniqueIds().add(
			ToGenerateUniqueId.builder().generateFrom(uig2)
				.label("SKU").build()
		);
		newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item2, user).getId();
		
		//		item1 = this.inventoryItemService.get(DEFAULT_TEST_DB_NAME, newId);
		//
		//		assertEquals(1, item.getUniqueIds().size());
		//
		//		UniqueId id = item.getUniqueIds().getFirst();
		//		log.info("generated id: {}", id);
		//		assertInstanceOf(GeneratedUniqueId.class, id);
	}
	
	@Test
	public void testOtherSameIdSameGeneratorUniqueIdInAdd() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item1 = this.getTestObject();
		InventoryItem item2 = this.getTestObject();
		
		ObjectId uig1 = this.uigs.add(DEFAULT_TEST_DB_NAME, IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc}").build(), user).getId();
		
		item1.getUniqueIds().add(
			ToGenerateUniqueId.builder().generateFrom(uig1)
				.label("SKU").build()
		);
		ObjectId newId = this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item1, user).getId();
		item1 = this.inventoryItemService.get(DEFAULT_TEST_DB_NAME, newId);
		
		item2.getUniqueIds().add(
			item1.getUniqueIds().getFirst()
		);
		assertThrows(ValidationException.class, ()->this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item2, user));
	}
	
	@Test
	public void testPricesInStats() {
		User user = this.getTestUserService().getTestUser();
		InventoryItem item = this.getTestObject();
		
		item.setStats((ItemStoredStats) new ItemStoredStats(item.getUnit())
											.setPrices(new LinkedHashSet<>() {{
												add(
													TotalPricing.builder()
														.label(FAKER.name().name())
														.totalPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
														.build()
												);
											}}));
		
		InsertOneResult result = this.inventoryItemService.getTypedCollection(DEFAULT_TEST_DB_NAME).insertOne(item);
		ObjectId newId = result.getInsertedId().asObjectId().getValue();
		
		
		InventoryItem itemGotten = this.inventoryItemService.get(DEFAULT_TEST_DB_NAME, newId);
		
		Document itemDoc = this.inventoryItemService.getDocumentCollection(DEFAULT_TEST_DB_NAME).find(Filters.eq("_id", newId)).first();
		
		log.info("item retrieved: {}", itemGotten);
		
		assertEquals(1, itemGotten.getStats().getPrices().size());
		
		TotalPricing price = itemGotten.getStats().getPrices().getFirst();
		
		assertNotNull(price.getTotalPrice());
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