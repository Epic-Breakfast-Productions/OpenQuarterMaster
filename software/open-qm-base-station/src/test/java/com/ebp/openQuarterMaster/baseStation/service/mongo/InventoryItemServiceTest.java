package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.data.InventoryItemTestObjectCreator;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.MongoServiceTest;
import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class InventoryItemServiceTest extends MongoServiceTest<InventoryItem, InventoryItemService> {
    @Inject
    InventoryItemService service;
    @Inject
    InventoryItemTestObjectCreator itemTestObjectCreator;

    @Override
    protected InventoryItem getTestObject() {
        return itemTestObjectCreator.getTestObject();
    }

    @AfterEach
    public void cleanup(){
        this.service.removeAll();
    }

    @Test
    public void injectTest(){
        assertNotNull(service);
    }

    @Test
    public void listTest(){
        this.defaultListTest(this.service);
    }
    @Test
    public void countTest(){
        this.defaultCountTest(this.service);
    }

    @Test
    public void addTest(){
        this.defaultAddTest(this.service);
    }

    @Test
    public void getObjectIdTest(){
        this.defaultGetObjectIdTest(this.service);
    }
    @Test
    public void getStringTest(){
        this.defaultGetStringTest(this.service);
    }
    @Test
    public void removeAllTest(){
        this.defaultRemoveAllTest(this.service);
    }



//    @Test
//    public void listTest(){
//
//    }
}