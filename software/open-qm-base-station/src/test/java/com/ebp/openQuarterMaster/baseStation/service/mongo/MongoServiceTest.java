package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestMainObject;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestMongoService;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestMongoServiceAllowNullUserCreate;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.history.EventType;
import com.ebp.openQuarterMaster.lib.core.history.HistoryEvent;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.Map;

import static com.ebp.openQuarterMaster.lib.core.Utils.OBJECT_MAPPER;
import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class MongoServiceTest extends RunningServerTest {

    @Inject
    TestMongoService testMongoService;

    //TODO:: config service; allowNullUserForCreate T/F
    //TODO:: do this properly with inject args?
    @Inject
    TestMongoServiceAllowNullUserCreate testMongoServiceAllowNullUserCreate;

    @Inject
    TestUserService testUserService;

    // <editor-fold desc="Count">
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
    // </editor-fold>

    // <editor-fold desc="Searching">
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
    // </editor-fold>

    // <editor-fold desc="Adding">
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
    // </editor-fold>

    // <editor-fold desc="Get">
    @Test
    public void testGetByObjectId(){
        TestMainObject original = new TestMainObject("hello world");
        testMongoServiceAllowNullUserCreate.add(original, null);

        TestMainObject gotten = testMongoServiceAllowNullUserCreate.get(original.getId());

        assertNotSame(original, gotten);
        assertEquals(original, gotten);
    }
    @Test
    public void testGetByString(){
        TestMainObject original = new TestMainObject("hello world");
        testMongoServiceAllowNullUserCreate.add(original, null);

        TestMainObject gotten = testMongoServiceAllowNullUserCreate.get(original.getId().toHexString());

        assertNotSame(original, gotten);
        assertEquals(original, gotten);
    }
    @Test
    public void testGetByNullObjectId(){
        TestMainObject original = new TestMainObject("hello world");
        testMongoServiceAllowNullUserCreate.add(original, null);

        TestMainObject gotten = testMongoServiceAllowNullUserCreate.get((ObjectId) null);

        assertNull(gotten);
    }
    @Test
    public void testGetByRandomObjectId(){
        TestMainObject original = new TestMainObject("hello world");
        testMongoServiceAllowNullUserCreate.add(original, null);

        TestMainObject gotten = testMongoServiceAllowNullUserCreate.get(ObjectId.get());

        assertNull(gotten);
    }
    // </editor-fold>

    // <editor-fold desc="Update">
    @Test
    public void testUpdate(){
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        ObjectNode update = OBJECT_MAPPER.createObjectNode();
        update.put("testField", "something completely different");
        ObjectNode atts = update.putObject("attributes");
        atts.put("some", "val");
        ArrayNode keywords = update.putArray("keywords");
        keywords.add("something");

        ZonedDateTime before = ZonedDateTime.now();

        TestMainObject result = this.testMongoService.update(original.getId(), update, user);

        ZonedDateTime after = ZonedDateTime.now();

        assertNotEquals(original, result);
        assertEquals("something completely different", result.getTestField());
        assertEquals(result.getAttributes(), Map.of("some", "val"));
        assertEquals(result.getKeywords(), List.of("something"));

        HistoryEvent ev = result.lastHistoryEvent();
        assertTrue(before.isBefore(ev.getTimestamp()));
        assertTrue(after.isAfter(ev.getTimestamp()));
        assertEquals(user.getId(), ev.getUserId());
    }
    @Test
    public void testUpdateWithString(){
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        ObjectNode update = OBJECT_MAPPER.createObjectNode();
        update.put("testField", "something completely different");
        ObjectNode atts = update.putObject("attributes");
        atts.put("some", "val");
        ArrayNode keywords = update.putArray("keywords");
        keywords.add("something");

        ZonedDateTime before = ZonedDateTime.now();

        TestMainObject result = this.testMongoService.update(original.getId().toHexString(), update, user);

        ZonedDateTime after = ZonedDateTime.now();

        assertNotEquals(original, result);
        assertEquals("something completely different", result.getTestField());
        assertEquals(result.getAttributes(), Map.of("some", "val"));
        assertEquals(result.getKeywords(), List.of("something"));

        HistoryEvent ev = result.lastHistoryEvent();
        assertEquals(EventType.UPDATE, ev.getType());
        assertTrue(before.isBefore(ev.getTimestamp()));
        assertTrue(after.isAfter(ev.getTimestamp()));
        assertEquals(user.getId(), ev.getUserId());
    }
    @Test
    public void testUpdateInvalidField(){
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        ObjectNode update = OBJECT_MAPPER.createObjectNode();
        update.put("invalidField", "something");

        assertThrows(
                IllegalArgumentException.class,
                ()->{
                    this.testMongoService.update(original.getId(), update, user);
                }
        );
    }

    @Test
    public void testUpdateBadFieldValue(){
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        ObjectNode update = OBJECT_MAPPER.createObjectNode();
        update.put("testField", "");

        assertThrows(
                IllegalArgumentException.class,
                ()->{
                    this.testMongoService.update(original.getId(), update, user);
                }
        );
    }

    @Test
    public void testUpdateHistory(){
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        ObjectNode update = OBJECT_MAPPER.createObjectNode();
        update.putObject("history");

        assertThrows(
                IllegalArgumentException.class,
                ()->{
                    this.testMongoService.update(original.getId(), update, user);
                }
        );
    }

    @Test
    public void testUpdateNullUser(){
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        ObjectNode update = OBJECT_MAPPER.createObjectNode();
        update.putObject("history");

        assertThrows(
                IllegalArgumentException.class,
                ()->{
                    this.testMongoService.update(original.getId(), update, null);
                }
        );
    }
    @Test
    public void testUpdateNonexistantObj(){
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        ObjectNode update = OBJECT_MAPPER.createObjectNode();
        update.putObject("history");

        assertThrows(
                IllegalArgumentException.class,
                ()->{
                    this.testMongoService.update(ObjectId.get(), update, null);
                }
        );
    }
    // </editor-fold>

    // <editor-fold desc="Remove">
    @Test
    public void testRemove() {
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        ZonedDateTime before = ZonedDateTime.now();

        TestMainObject result = this.testMongoService.remove(original.getId(), user);

        ZonedDateTime after = ZonedDateTime.now();

        assertEquals(0, this.testMongoService.count());

        assertEquals(original.getId(), result.getId());
        assertEquals(original.getTestField(), result.getTestField());


        HistoryEvent ev = result.lastHistoryEvent();
        assertEquals(EventType.REMOVE, ev.getType());
        assertTrue(before.isBefore(ev.getTimestamp()));
        assertTrue(after.isAfter(ev.getTimestamp()));
        assertEquals(user.getId(), ev.getUserId());
    }
    @Test
    public void testRemoveWithString() {
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        ZonedDateTime before = ZonedDateTime.now();

        TestMainObject result = this.testMongoService.remove(original.getId().toHexString(), user);

        ZonedDateTime after = ZonedDateTime.now();

        assertEquals(0, this.testMongoService.count());

        assertEquals(original.getId(), result.getId());
        assertEquals(original.getTestField(), result.getTestField());


        HistoryEvent ev = result.lastHistoryEvent();
        assertEquals(EventType.REMOVE, ev.getType());
        assertTrue(before.isBefore(ev.getTimestamp()));
        assertTrue(after.isAfter(ev.getTimestamp()));
        assertEquals(user.getId(), ev.getUserId());
    }
    @Test
    public void testRemoveNullUser() {
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    this.testMongoService.remove(original.getId().toHexString(), null);
                }
        );
        assertEquals(1, this.testMongoService.count());
    }

    @Test
    public void testRemoveNullId() {
        User user = this.testUserService.getTestUser();
        TestMainObject original = new TestMainObject("hello world");
        testMongoService.add(original, user);

        assertNull(this.testMongoService.remove((ObjectId) null, user));
        assertEquals(1, this.testMongoService.count());
    }
    // </editor-fold>

}