package com.ebp.openQuarterMaster.baseStation.data.mongo.items;

import com.ebp.openQuarterMaster.baseStation.data.mongo.MongoEntityTest;
import com.ebp.openQuarterMaster.lib.core.InventoryItem;
import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
public class InventoryItemEntityTest extends MongoEntityTest<InventoryItemEntity, InventoryItem> {

    @Override
    public InventoryItemEntity getBasicTestEntity() {
        return null;
    }

    @Override
    public InventoryItem getBasicTestObj() {
        return InventoryItem
                .builder()
                .name("test item")
                .build();
    }

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
    }


    @Test
    public void testPersist() {
        InventoryItemEntity entityOne = new InventoryItemEntity();
        entityOne.setObj(this.getBasicTestObj());

        entityOne.persist();
    }

}
