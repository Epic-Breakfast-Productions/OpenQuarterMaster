package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.testResources.data.InventoryItemTestObjectCreator;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.MongoHistoriedServiceTest;
import com.ebp.openQuarterMaster.lib.core.storage.items.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.SimpleAmountItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.AmountStored;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.units.indriya.quantity.Quantities;

import javax.inject.Inject;
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
	public void testAddComplexObj(){
		SimpleAmountItem item = (SimpleAmountItem) new SimpleAmountItem().setName(FAKER.commerce().productName());
		item.getStorageMap().put(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(0, item.getUnit())));
		
		ObjectId id = this.inventoryItemService.add(item, this.testUserService.getTestUser());
		
		assertNotNull(item.getId());
		assertEquals(id, item.getId());
		
		List<InventoryItem> list = inventoryItemService.list();
		log.info("num in collection: {}", list.size());
		assertEquals(1, list.size(), "Unexpected number of objects in collection.");
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
	
	
	//    @Test
	//    public void listTest(){
	//
	//    }
}