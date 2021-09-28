package com.ebp.openQuarterMaster.baseStation.data.mongo.storage;

import com.ebp.openQuarterMaster.baseStation.data.pojos.StorageSpace;
import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
public class StorageBlockTest extends RunningServerTest {

    //    @Test
    public void testEquals() {
        StorageSpace entityOne = new StorageSpace();
        StorageSpace entityTwo = new StorageSpace();


        assertEquals(
                entityOne,
                entityTwo
        );

//        entityOne.persist();
//        entityTwo.persist();

        assertNotEquals(
                entityOne,
                entityTwo
        );
        //TODO:: more
    }
}
