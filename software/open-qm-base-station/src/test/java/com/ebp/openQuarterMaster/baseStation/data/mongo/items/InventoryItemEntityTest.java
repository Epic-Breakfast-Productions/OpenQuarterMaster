package com.ebp.openQuarterMaster.baseStation.data.mongo.items;

import com.ebp.openQuarterMaster.baseStation.data.pojos.InventoryItemTest;
import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
public class InventoryItemEntityTest extends RunningServerTest {

    @Test
    public void testEquals() {

        InventoryItemEntity entityOne = new InventoryItemEntity();
        InventoryItemEntity entityTwo = new InventoryItemEntity();

        log.info("{}", entityOne);

        assertEquals(
                entityOne,
                entityTwo
        );

        entityOne.persist();
        entityTwo.persist();

        assertNotEquals(
                entityOne,
                entityTwo
        );
        //TODO:: more
    }


    @Test
    public void testPersist() {
        InventoryItemEntity entityOne = new InventoryItemEntity();
        entityOne.setObj(InventoryItemTest.getTestItem());

        entityOne.persist();
    }
}
