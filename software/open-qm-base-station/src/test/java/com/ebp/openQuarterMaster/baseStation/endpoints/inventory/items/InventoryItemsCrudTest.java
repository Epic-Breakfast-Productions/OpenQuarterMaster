package com.ebp.openQuarterMaster.baseStation.endpoints.inventory.items;

import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class InventoryItemsCrudTest extends RunningServerTest {
    //TODO

}