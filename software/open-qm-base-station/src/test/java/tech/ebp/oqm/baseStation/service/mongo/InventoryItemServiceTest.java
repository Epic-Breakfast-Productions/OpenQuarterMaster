package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.ListAmountItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.TrackedItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;

import jakarta.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class InventoryItemServiceTest extends MongoHistoriedServiceTest<InventoryItem, InventoryItemService> {
	
	InventoryItemService inventoryItemService;
	
	InventoryItemTestObjectCreator itemTestObjectCreator;
	
	@Inject
	InventoryItemServiceTest(
		InventoryItemService inventoryItemService,
		InventoryItemTestObjectCreator itemTestObjectCreator,
		TestUserService testUserService
	) {
		this.inventoryItemService = inventoryItemService;
		this.itemTestObjectCreator = itemTestObjectCreator;
		this.testUserService = testUserService;
	}
	
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
	public void testAddSimpleAmountItem(){
		SimpleAmountItem item = (SimpleAmountItem) new SimpleAmountItem().setName(FAKER.commerce().productName());
		item.getStorageMap().put(ObjectId.get(), new SingleAmountStoredWrapper(new AmountStored(0, item.getUnit())));
		
		ObjectId id = this.inventoryItemService.add(item, this.testUserService.getTestUser());
		
		assertNotNull(item.getId());
		assertEquals(id, item.getId());
		
		List<InventoryItem> list = inventoryItemService.list();
		log.info("num in collection: {}", list.size());
		assertEquals(1, list.size(), "Unexpected number of objects in collection.");
	}
	
	@Test
	public void testAddListAmountItem(){
		ListAmountItem item = (ListAmountItem) new ListAmountItem().setName(FAKER.commerce().productName());
		item.add(ObjectId.get(), new AmountStored(0, item.getUnit()));
		
		ObjectId id = this.inventoryItemService.add(item, this.testUserService.getTestUser());
		
		assertNotNull(item.getId());
		assertEquals(id, item.getId());
		
		List<InventoryItem> list = inventoryItemService.list();
		log.info("num in collection: {}", list.size());
		assertEquals(1, list.size(), "Unexpected number of objects in collection.");
	}
	@Test
	public void testAddTrackedItem(){
		TrackedItem item = (TrackedItem) new TrackedItem()
											 .setTrackedItemIdentifierName("id")
											 .setName(FAKER.commerce().productName());
		item.add(ObjectId.get(), new TrackedStored(FAKER.commerce().productName()));
		
		ObjectId id = this.inventoryItemService.add(item, this.testUserService.getTestUser());
		
		assertNotNull(item.getId());
		assertEquals(id, item.getId());
		
		List<InventoryItem> list = inventoryItemService.list();
		log.info("num in collection: {}", list.size());
		assertEquals(1, list.size(), "Unexpected number of objects in collection.");
	}
	
	@Test
	public void testUpdateSimpleAmountItemJson(){
		SimpleAmountItem item = (SimpleAmountItem) new SimpleAmountItem().setName(FAKER.commerce().productName());
		item.getStorageMap().put(ObjectId.get(), new SingleAmountStoredWrapper(new AmountStored(0, item.getUnit())));
		ObjectId id = this.inventoryItemService.add(item, this.testUserService.getTestUser());
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.valueToTree(item);
		String newName = item.getName() + " new";
		
		update.put("name", newName);
		
		SimpleAmountItem item2 = (SimpleAmountItem) this.inventoryItemService.update(id, update);
		
		assertEquals(newName, item2.getName());
	}
	
	@Test
	public void testUpdateListAmountItemJson(){
		ListAmountItem item = (ListAmountItem) new ListAmountItem().setName(FAKER.commerce().productName());
		item.add(ObjectId.get(), new AmountStored(0, item.getUnit()));
		ObjectId id = this.inventoryItemService.add(item, this.testUserService.getTestUser());
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.valueToTree(item);
		String newName = item.getName() + " new";
		
		update.put("name", newName);
		
		ListAmountItem item2 = (ListAmountItem) this.inventoryItemService.update(id, update);
		
		assertEquals(newName, item2.getName());
	}
	
	@Test
	public void testUpdateTrackedItemJson(){
		TrackedItem item = (TrackedItem) new TrackedItem()
											 .setTrackedItemIdentifierName("id")
											 .setName(FAKER.commerce().productName());
		item.add(ObjectId.get(), new TrackedStored(FAKER.commerce().productName()));
		ObjectId id = this.inventoryItemService.add(item, this.testUserService.getTestUser());
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.valueToTree(item);
		String newName = item.getName() + " new";
		
		update.put("name", newName);
		
		TrackedItem item2 = (TrackedItem) this.inventoryItemService.update(id, update);
		
		assertEquals(newName, item2.getName());
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
	
	//TODO:: test expiry related
	
	//    @Test
	//    public void listTest(){
	//
	//    }
}