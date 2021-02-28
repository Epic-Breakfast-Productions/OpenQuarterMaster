package com.ebp.openQuarterMaster.baseStation.data.items;

import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static tec.units.ri.unit.Units.LITRE;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
public class InventoryItemTest extends RunningServerTest {

    @Test
    public void testEquals() {
        InventoryItem entityOne = new InventoryItem();
        InventoryItem entityTwo = new InventoryItem();

        entityOne.setCapMeasurement(LITRE);
        entityOne.setCapMeasurement(LITRE);

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
}
