package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.testResources.data.InventoryItemTestObjectCreator;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.MongoServiceTest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class InventoryItemServiceTest extends MongoServiceTest<InventoryItem, InventoryItemService> {
	
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
		SimpleAmountItem item = (SimpleAmountItem) new SimpleAmountItem(){{
			this.getStorageMap().put(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(0, this.getUnit())));
		}}.setName(FAKER.commerce().productName());
		
		ObjectId id = this.inventoryItemService.add(item, this.testUserService.getTestUser());
		
		assertNotNull(item.getId());
		assertEquals(id, item.getId());
		
		log.info("num in collection: {}", inventoryItemService.list().size());
		assertEquals(1, inventoryItemService.list().size());
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