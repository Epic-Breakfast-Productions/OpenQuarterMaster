package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestMainObject;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestMongoService;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestMongoServiceAllowNullUserCreate;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.history.EventType;
import com.ebp.openQuarterMaster.lib.core.history.HistoryEvent;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.mongodb.client.model.Sorts;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class MongoServiceTest extends RunningServerTest {

    //TODO:: config service; allowNullUserForCreate T/F

    @Inject
    TestMongoService testMongoService;

    //TODO:: do this properly with inject args?
    @Inject
    TestMongoServiceAllowNullUserCreate testMongoServiceAllowNullUserCreate;

    @Inject
    TestUserService testUserService;

    @Test
    public void testCollectionEmpty() {
        assertTrue(this.testMongoService.collectionEmpty());

        this.testMongoService.add(new TestMainObject(), this.testUserService.getTestUser());

        assertFalse(this.testMongoService.collectionEmpty());
    }

    @Test
    public void testCollectionCount() {
        this.testMongoService.add(new TestMainObject(), this.testUserService.getTestUser());

        assertEquals(1, this.testMongoService.count());

        for (int i = 0; i < 5; i++) {
            this.testMongoService.add(new TestMainObject().setTestField("hello"), this.testUserService.getTestUser());
        }

        assertEquals(6, this.testMongoService.count());
        assertEquals(5, this.testMongoService.count(eq("testField", "hello")));
    }

    @Test
    public void testList() {
        for (int i = 0; i < 5; i++) {
            this.testMongoService.add(new TestMainObject().setTestField("" + i), this.testUserService.getTestUser());
        }

        assertEquals(5, this.testMongoService.list().size());
        assertEquals(this.testMongoService.count(), this.testMongoService.list().size());
    }

    @Test
    public void testListWithArgs() {
        for (int i = 4; i >= 0; i--) {
            this.testMongoService.add(new TestMainObject().setTestField("" + i), this.testUserService.getTestUser());
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

    //TODO:: get, update, remove

    @Test
    public void testAddNullObj(){
        Assert.assertThrows(
                NullPointerException.class,
                ()-> this.testMongoService.add(null, new User())
        );
    }
    @Test
    public void testAddWithHistory(){
        Assert.assertThrows(
                IllegalArgumentException.class,
                ()->{
                    this.testMongoService.add(
                            (TestMainObject) new TestMainObject().setHistory(List.of(new HistoryEvent())),
                            this.testUserService.getTestUser()
                    );
                }
        );
    }

    @Test
    public void testAddWithoutUserWhenNotAllowed(){
        Assert.assertThrows(
                MongoService.NULL_USER_EXCEPT_MESSAGE,
                IllegalArgumentException.class,
                ()->{
                    this.testMongoService.add(
                            new TestMainObject(),
                            null
                    );
                }
        );
    }

    @Test
    public void testAdd(){
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("Hello world");
        ZonedDateTime start = ZonedDateTime.now();

        ObjectId returned = this.testMongoService.add(
                original,
                user
        );
        ZonedDateTime after = ZonedDateTime.now();

        assertEquals(1, this.testMongoService.count());

        assertNotNull(original.getId());
        assertEquals(returned, original.getId());

        assertEquals(1, original.getHistory().size());
        HistoryEvent event = original.getHistory().get(0);
        assertEquals(EventType.CREATE, event.getType());
        assertEquals(user.getId(), event.getUserId());
        assertTrue(start.isBefore(event.getTimestamp()));
        assertTrue(after.isAfter(event.getTimestamp()));

        TestMainObject gotten = this.testMongoService.get(original.getId());

        assertEquals(original, gotten);
    }

    @Test
    public void testAddWithoutUserAllowed(){
        TestMainObject original = new TestMainObject("Hello world");
        ZonedDateTime start = ZonedDateTime.now();

        ObjectId returned = this.testMongoServiceAllowNullUserCreate.add(
                original,
                null
        );
        ZonedDateTime after = ZonedDateTime.now();

        assertEquals(1, this.testMongoService.count());

        assertNotNull(original.getId());
        assertEquals(returned, original.getId());

        assertEquals(1, original.getHistory().size());
        HistoryEvent event = original.getHistory().get(0);
        assertEquals(EventType.CREATE, event.getType());
        assertNull(event.getUserId());
        assertTrue(start.isBefore(event.getTimestamp()));
        assertTrue(after.isAfter(event.getTimestamp()));

        TestMainObject gotten = this.testMongoService.get(original.getId());

        assertEquals(original, gotten);
    }

}