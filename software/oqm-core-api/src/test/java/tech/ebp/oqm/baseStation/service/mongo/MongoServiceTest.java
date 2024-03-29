package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.mongodb.client.model.Sorts;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.model.collectionStats.CollectionStats;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingOptions;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.testResources.data.TestMainObject;
import tech.ebp.oqm.baseStation.testResources.data.TestMainObjectSearch;
import tech.ebp.oqm.baseStation.testResources.data.TestMongoService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;

import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class MongoServiceTest extends RunningServerTest {
	
	@Inject
	TestMongoService testMongoService;
	
	// <editor-fold desc="Count">
	@Test
	public void testCollectionEmpty() {
		assertTrue(this.testMongoService.collectionEmpty());
		
		this.testMongoService.add(new TestMainObject(FAKER.name().name()));
		
		assertFalse(this.testMongoService.collectionEmpty());
	}
	
	@Test
	public void testCollectionCount() {
		this.testMongoService.add(new TestMainObject(FAKER.name().name()));
		
		assertEquals(1, this.testMongoService.count());
		
		for (int i = 0; i < 5; i++) {
			this.testMongoService.add(new TestMainObject().setTestField("hello"));
		}
		
		assertEquals(6, this.testMongoService.count());
		assertEquals(5, this.testMongoService.count(eq("testField", "hello")));
	}
	// </editor-fold>
	
	// <editor-fold desc="Searching">
	@Test
	public void testList() {
		List<TestMainObject> originals = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			TestMainObject cur = new TestMainObject().setTestField("" + i);
			this.testMongoService.add(cur);
			originals.add(cur);
		}
		
		assertEquals(5, this.testMongoService.list().size());
		assertEquals(this.testMongoService.count(), this.testMongoService.list().size());
		assertEquals(Lists.reverse(originals), this.testMongoService.list());
	}
	
	@Test
	public void testListWithArgs() {
		for (int i = 4; i >= 0; i--) {
			this.testMongoService.add(new TestMainObject().setTestField("" + i));
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
	
	@Test
	public void testSearchSingleAtt() {
		for (int i = 4; i >= 0; i--) {
			this.testMongoService.add(
				(TestMainObject) new TestMainObject()
									 .setTestField("" + i)
									 .setAttributes(Map.of("key", "" + i))
									 .setKeywords(List.of("" + i))
			);
		}
		//test single attribute
		SearchResult<TestMainObject> result = this.testMongoService.search(
			(TestMainObjectSearch) new TestMainObjectSearch()
									   .setAttributeKeys(List.of("key"))
									   .setAttributeValues(List.of("" + 3))
			,
			true
		);
		assertEquals(1, result.getNumResultsForEntireQuery());
		assertEquals("" + 3, result.getResults().get(0).getTestField());
	}
	
	@Test
	public void testSearchKeyword() {
		for (int i = 4; i >= 0; i--) {
			this.testMongoService.add(
				(TestMainObject) new TestMainObject()
									 .setTestField("" + i)
									 .setAttributes(Map.of("key", "" + i))
									 .setKeywords(List.of("" + i))
			);
		}
		
		SearchResult<TestMainObject> result = this.testMongoService.search(
			(TestMainObjectSearch) new TestMainObjectSearch()
									   .setKeywords(List.of("" + 3))
			,
			true
		);
		assertEquals(1, result.getNumResultsForEntireQuery());
		assertEquals("" + 3, result.getResults().get(0).getTestField());
	}
	
	@Test
	public void testSearchSingleAttSingleKeyword() {
		for (int i = 4; i >= 0; i--) {
			this.testMongoService.add(
				(TestMainObject) new TestMainObject()
									 .setTestField("" + i)
									 .setAttributes(Map.of("key", "" + i))
									 .setKeywords(List.of("" + i))
			);
		}
		//test single attribute
		SearchResult<TestMainObject> result = this.testMongoService.search(
			(TestMainObjectSearch) new TestMainObjectSearch()
									   .setAttributeKeys(List.of("key"))
									   .setAttributeValues(List.of("" + 3))
									   .setKeywords(List.of("" + 3))
			,
			true
		);
		assertEquals(1, result.getNumResultsForEntireQuery());
		assertEquals("" + 3, result.getResults().get(0).getTestField());
	}
	// </editor-fold>
	
	// <editor-fold desc="Adding">
	@Test
	public void testAddNullObj() {
		Assert.assertThrows(
			NullPointerException.class,
			()->this.testMongoService.add(null)
		);
	}
	
	@Test
	public void testAdd() {
		TestMainObject original = new TestMainObject("Hello world");
		
		ObjectId returned = this.testMongoService.add(original);
		
		assertEquals(1, this.testMongoService.count());
		
		assertNotNull(original.getId());
		assertEquals(returned, original.getId());
		
		TestMainObject gotten = this.testMongoService.get(original.getId());
		
		assertEquals(original, gotten);
	}
	
	@Test
	public void testAddBulk() {
		List<TestMainObject> originals = new ArrayList<>();
		
		for (int i = 1; i <= 5; i++) {
			originals.add(new TestMainObject("Hello world " + i));
		}
		
		List<ObjectId> returned = this.testMongoService.addBulk(originals);
		
		assertEquals(originals.size(), this.testMongoService.count());
		
		for (TestMainObject original : originals) {
			assertNotNull(original.getId());
			
			assertTrue(returned.contains(original.getId()));
			
			TestMainObject gotten = this.testMongoService.get(original.getId());
			
			assertEquals(original, gotten);
		}
	}
	
	@Test
	public void testAddBulkError() {
		List<TestMainObject> originals = new ArrayList<>();
		
		for (int i = 1; i <= 5; i++) {
			originals.add(new TestMainObject("Hello world " + i));
		}
		originals.add(new TestMainObject());
		
		log.info("Originals: {}", originals);
		
		assertThrows(ValidationException.class, ()->this.testMongoService.addBulk(originals));
		
		assertEquals(0, this.testMongoService.count());
	}
	
	@Test
	public void testAddBulkError2() {
		List<TestMainObject> originals = new ArrayList<>();
		
		originals.add(new TestMainObject());
		for (int i = 1; i <= 5; i++) {
			originals.add(new TestMainObject("Hello world " + i));
		}
		
		log.info("Originals: {}", originals);
		
		assertThrows(ValidationException.class, ()->this.testMongoService.addBulk(originals));
		
		assertEquals(0, this.testMongoService.count());
	}
	// </editor-fold>
	
	// <editor-fold desc="Get">
	@Test
	public void testGetByObjectId() {
		TestMainObject original = new TestMainObject("hello world");
		this.testMongoService.add(original);
		
		TestMainObject gotten = this.testMongoService.get(original.getId());
		
		assertNotSame(original, gotten);
		assertEquals(original, gotten);
	}
	
	@Test
	public void testGetByString() {
		TestMainObject original = new TestMainObject("hello world");
		this.testMongoService.add(original);
		
		TestMainObject gotten = this.testMongoService.get(original.getId().toHexString());
		
		assertNotSame(original, gotten);
		assertEquals(original, gotten);
	}
	
	@Test
	public void testGetByNullObjectId() {
		TestMainObject original = new TestMainObject("hello world");
		this.testMongoService.add(original);
		
		assertThrows(
			DbNotFoundException.class,
			()->{
				this.testMongoService.get((ObjectId) null);
			}
		);
	}
	
	@Test
	public void testGetByRandomObjectId() {
		TestMainObject original = new TestMainObject("hello world");
		this.testMongoService.add(original);
		
		assertThrows(
			DbNotFoundException.class,
			()->{
				this.testMongoService.get(ObjectId.get());
			}
		);
	}
	// </editor-fold>
	
	// <editor-fold desc="Update">
	@Test
	public void testUpdate() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.put("testField", "something completely different");
		ObjectNode atts = update.putObject("attributes");
		atts.put("some", "val");
		ArrayNode keywords = update.putArray("keywords");
		keywords.add("something");
		
		TestMainObject result = this.testMongoService.update(original.getId(), update);
		
		assertNotEquals(original, result);
		assertEquals("something completely different", result.getTestField());
		assertEquals(result.getAttributes(), Map.of("some", "val"));
		assertEquals(result.getKeywords(), List.of("something"));
	}
	
	@Test
	public void testUpdateWithString() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.put("testField", "something completely different");
		ObjectNode atts = update.putObject("attributes");
		atts.put("some", "val");
		ArrayNode keywords = update.putArray("keywords");
		keywords.add("something");
		
		TestMainObject result = this.testMongoService.update(original.getId().toHexString(), update);
		
		assertNotEquals(original, result);
		assertEquals("something completely different", result.getTestField());
		assertEquals(result.getAttributes(), Map.of("some", "val"));
		assertEquals(result.getKeywords(), List.of("something"));
	}
	
	@Test
	public void testUpdateInvalidField() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.put("invalidField", "something");
		
		assertThrows(
			IllegalArgumentException.class,
			()->{
				this.testMongoService.update(original.getId(), update);
			}
		);
	}
	
	@Test
	public void testUpdateId() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.put("id", ObjectId.get().toHexString());
		
		assertThrows(
			IllegalArgumentException.class,
			()->{
				this.testMongoService.update(original.getId(), update);
			}
		);
	}
	
	@Test
	public void testUpdateBadFieldValue() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.put("testField", "");
		
		assertThrows(
			IllegalArgumentException.class,
			()->{
				this.testMongoService.update(original.getId(), update);
			}
		);
	}
	
	@Test
	public void testUpdateHistory() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.putObject("history");
		
		assertThrows(
			IllegalArgumentException.class,
			()->{
				this.testMongoService.update(original.getId(), update);
			}
		);
	}
	
	@Test
	public void testUpdateNonexistantObj() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.putObject("history");
		
		assertThrows(
			DbNotFoundException.class,
			()->{
				this.testMongoService.update(ObjectId.get(), update);
			}
		);
	}
	// </editor-fold>
	
	// <editor-fold desc="Remove">
	@Test
	public void testRemove() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(original);
		
		TestMainObject result = this.testMongoService.remove(original.getId());
		
		assertEquals(0, this.testMongoService.count());
		assertEquals(original.getId(), result.getId());
		assertEquals(original.getTestField(), result.getTestField());
	}
	
	@Test
	public void testRemoveWithString() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(original);
		
		TestMainObject result = this.testMongoService.remove(original.getId().toHexString());
		
		assertEquals(0, this.testMongoService.count());
		assertEquals(original.getId(), result.getId());
		assertEquals(original.getTestField(), result.getTestField());
	}
	
	@Test
	public void testRemoveNullId() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(original);
		
		assertThrows(
			DbNotFoundException.class,
			()->{
				this.testMongoService.remove((ObjectId) null);
			}
		);
		
		assertEquals(1, this.testMongoService.count());
	}
	// </editor-fold>
	
	@Test
	public void testSumIntFieldIntegerEmpty() {
		assertEquals(0L, this.testMongoService.getSumOfIntField("intValue"));
	}
	
	@Test
	public void testSumIntFieldIntegerSmall() {
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), 5));
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), 5));
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), 5));
		
		assertEquals(15L, this.testMongoService.getSumOfIntField("intValue"));
	}
	
	@Test
	public void testSumIntFieldIntegerLarge() {
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), Integer.MAX_VALUE));
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), Integer.MAX_VALUE));
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), Integer.MAX_VALUE));
		
		assertEquals(
			(long) Integer.MAX_VALUE * 3L,
			this.testMongoService.getSumOfIntField("intValue")
		);
	}
	
	@Test
	public void testSumIntFieldLong() {
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), (long) Integer.MAX_VALUE));
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), (long) Integer.MAX_VALUE));
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), (long) Integer.MAX_VALUE));
		
		assertEquals(
			(long) Integer.MAX_VALUE * 3L,
			this.testMongoService.getSumOfIntField("longValue")
		);
	}
	
	@Test
	public void testSumFloatFieldEmpty() {
		assertEquals(
			0.0,
			this.testMongoService.getSumOfFloatField("floatValue")
		);
	}
	
	@Test
	public void testSumFloatField() {
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), 5.5));
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), 5.5));
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), 5.5));
		
		assertEquals(
			16.5,
			this.testMongoService.getSumOfFloatField("floatValue")
		);
	}
	
	@Test
	public void testCo() {
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), 5.5));
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), 5.5));
		this.testMongoService.add(new TestMainObject(FAKER.name().name(), 5.5));
		
		assertEquals(
			CollectionStats.builder().size(3).build(),
			this.testMongoService.getStats()
		);
	}
	
}