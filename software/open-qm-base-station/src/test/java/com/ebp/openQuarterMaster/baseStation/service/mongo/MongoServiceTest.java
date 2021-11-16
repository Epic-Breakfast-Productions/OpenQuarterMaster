package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestMainObject;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestMongoService;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.mongodb.client.model.Sorts;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class MongoServiceTest extends RunningServerTest {

    @Inject
    TestMongoService testMongoService;

    @Test
    public void testCollectionEmpty() {
        assertTrue(this.testMongoService.collectionEmpty());

        this.testMongoService.add(new TestMainObject(), null);

        assertFalse(this.testMongoService.collectionEmpty());
    }

    @Test
    public void testCollectionCount() {
        this.testMongoService.add(new TestMainObject(), null);

        assertEquals(1, this.testMongoService.count());

        for (int i = 0; i < 5; i++) {
            this.testMongoService.add(new TestMainObject().setTestField("hello"), null);
        }

        assertEquals(6, this.testMongoService.count());
        assertEquals(5, this.testMongoService.count(eq("testField", "hello")));
    }

    @Test
    public void testList() {
        for (int i = 0; i < 5; i++) {
            this.testMongoService.add(new TestMainObject().setTestField("" + i), null);
        }

        assertEquals(5, this.testMongoService.list().size());
        assertEquals(this.testMongoService.count(), this.testMongoService.list().size());
    }

    @Test
    public void testListWithArgs() {
        for (int i = 4; i >= 0; i--) {
            this.testMongoService.add(new TestMainObject().setTestField("" + i), null);
        }
        //filter
        Bson filter = eq("testField", "" + 1);
        assertEquals(1, this.testMongoService.list(filter, null, null).size());
        assertEquals(this.testMongoService.count(filter), this.testMongoService.list(filter, null, null).size());

        //sort
        List<TestMainObject> result = this.testMongoService.list(null, Sorts.ascending("testField"), null);
        int j = 0;
        for (TestMainObject cur : result) {
            assertEquals("" + j, cur.getTestField());
            j++;
        }

        //paging
        for (int i = 4; i >= 0; i--) {
            result = this.testMongoService.list(null, Sorts.ascending("testField"), new PagingOptions(1, i + 1));
            assertEquals(1, result.size());
            assertEquals("" + i, result.get(0).getTestField());
        }
    }

    //TODO:: get, update, add, remove

}