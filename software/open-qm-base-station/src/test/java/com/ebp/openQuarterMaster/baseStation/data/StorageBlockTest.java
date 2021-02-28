package com.ebp.openQuarterMaster.baseStation.data;

import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
public class StorageBlockTest extends RunningServerTest {

    @Test
    public void testEquals() {
        StorageBlock entityOne = new StorageBlock();
        StorageBlock entityTwo = new StorageBlock();


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
