package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.testResources.data.InventoryItemTestObjectCreator;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.MongoServiceTest;
import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

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