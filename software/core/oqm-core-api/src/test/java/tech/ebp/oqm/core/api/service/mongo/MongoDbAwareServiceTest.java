package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.mongodb.client.model.Sorts;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.search.PagingOptions;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.testResources.data.TestMainObject;
import tech.ebp.oqm.core.api.testResources.data.TestMainObjectSearch;
import tech.ebp.oqm.core.api.testResources.data.TestMongoService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;

import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
class MongoDbAwareServiceTest extends RunningServerTest {
	
	@Inject
	TestMongoService testMongoService;
	
	// <editor-fold desc="Count">
	@Test
	public void testCollectionEmpty() {
		assertTrue(this.testMongoService.collectionEmpty(DEFAULT_TEST_DB_NAME));
		
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name()));
		
		assertFalse(this.testMongoService.collectionEmpty(DEFAULT_TEST_DB_NAME));
	}
	
	@Test
	public void testCollectionCount() {
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name()));
		
		assertEquals(1, this.testMongoService.count(DEFAULT_TEST_DB_NAME));
		
		for (int i = 0; i < 5; i++) {
			this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject().setTestField("hello"));
		}
		
		assertEquals(6, this.testMongoService.count(DEFAULT_TEST_DB_NAME));
		assertEquals(5, this.testMongoService.count(DEFAULT_TEST_DB_NAME, eq("testField", "hello")));
	}
	// </editor-fold>
	
	// <editor-fold desc="Searching">
	@Test
	public void testList() {
		List<TestMainObject> originals = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			TestMainObject cur = new TestMainObject().setTestField("" + i);
			this.testMongoService.add(DEFAULT_TEST_DB_NAME, cur);
			originals.add(cur);
		}
		
		assertEquals(5, this.testMongoService.list(DEFAULT_TEST_DB_NAME).size());
		assertEquals(this.testMongoService.count(DEFAULT_TEST_DB_NAME), this.testMongoService.list(DEFAULT_TEST_DB_NAME).size());
		assertEquals(Lists.reverse(originals), this.testMongoService.list(DEFAULT_TEST_DB_NAME));
	}
	
	@Test
	public void testListWithArgs() {
		for (int i = 4; i >= 0; i--) {
			this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject().setTestField("" + i));
		}
		//filter
		Bson filter = eq("testField", "" + 1);
		assertEquals(1, this.testMongoService.list(DEFAULT_TEST_DB_NAME, filter, null, null).size());
		assertEquals(this.testMongoService.count(DEFAULT_TEST_DB_NAME, filter), this.testMongoService.list(DEFAULT_TEST_DB_NAME, filter, null, null).size());
		
		//sort
		List<TestMainObject> result = this.testMongoService.list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("testField"), null);
		int j = 0;
		for (TestMainObject cur : result) {
			assertEquals("" + j, cur.getTestField());
			j++;
		}
		
		//paging
		for (int i = 4; i >= 0; i--) {
			result = this.testMongoService.list(DEFAULT_TEST_DB_NAME, null, Sorts.ascending("testField"), new PagingOptions(1, i + 1));
			assertEquals(1, result.size());
			assertEquals("" + i, result.get(0).getTestField());
		}
	}
	
	@Test
	public void testSearchSingleAtt() {
		for (int i = 4; i >= 0; i--) {
			this.testMongoService.add(
				DEFAULT_TEST_DB_NAME,
				(TestMainObject) new TestMainObject()
									 .setTestField("" + i)
									 .setAttributes(Map.of("key", "" + i))
									 .setKeywords(List.of("" + i))
			);
		}
		//test single attribute
		SearchResult<TestMainObject> result = this.testMongoService.search(
			DEFAULT_TEST_DB_NAME,
			(TestMainObjectSearch) new TestMainObjectSearch()
									   .setAttributeKeys(List.of("key"))
									   .setAttributeValues(List.of("" + 3))
		);
		assertEquals(1, result.getNumResultsForEntireQuery());
		assertEquals("" + 3, result.getResults().get(0).getTestField());
	}
	
	@Test
	public void testSearchKeyword() {
		for (int i = 4; i >= 0; i--) {
			this.testMongoService.add(
				DEFAULT_TEST_DB_NAME,
				(TestMainObject) new TestMainObject()
									 .setTestField("" + i)
									 .setAttributes(Map.of("key", "" + i))
									 .setKeywords(List.of("" + i))
			);
		}
		
		SearchResult<TestMainObject> result = this.testMongoService.search(
			DEFAULT_TEST_DB_NAME,
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
				DEFAULT_TEST_DB_NAME,
				(TestMainObject) new TestMainObject()
									 .setTestField("" + i)
									 .setAttributes(Map.of("key", "" + i))
									 .setKeywords(List.of("" + i))
			);
		}
		//test single attribute
		SearchResult<TestMainObject> result = this.testMongoService.search(
			DEFAULT_TEST_DB_NAME,
			(TestMainObjectSearch) new TestMainObjectSearch()
									   .setAttributeKeys(List.of("key"))
									   .setAttributeValues(List.of("" + 3))
									   .setKeywords(List.of("" + 3))
		);
		assertEquals(1, result.getNumResultsForEntireQuery());
		assertEquals("" + 3, result.getResults().get(0).getTestField());
	}
	// </editor-fold>
	
	// <editor-fold desc="Adding">
	@Test
	public void testAddNullObj() {
		assertThrows(
			NullPointerException.class,
			()->this.testMongoService.add(DEFAULT_TEST_DB_NAME, null)
		);
	}
	
	@Test
	public void testAdd() {
		TestMainObject original = new TestMainObject("Hello world");
		
		TestMainObject returned = this.testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		assertEquals(1, this.testMongoService.count(DEFAULT_TEST_DB_NAME));
		
		assertNotNull(original.getId());
		assertEquals(returned, original);
		
		TestMainObject gotten = this.testMongoService.get(DEFAULT_TEST_DB_NAME, original.getId());
		
		assertEquals(original, gotten);
	}
	
	@Test
	public void testAddBulk() {
		List<TestMainObject> originals = new ArrayList<>();
		
		for (int i = 1; i <= 5; i++) {
			originals.add(new TestMainObject("Hello world " + i));
		}
		
		List<TestMainObject> returned = this.testMongoService.addBulk(DEFAULT_TEST_DB_NAME, originals);
		
		assertEquals(originals.size(), this.testMongoService.count(DEFAULT_TEST_DB_NAME));
		
		for (TestMainObject original : originals) {
			assertNotNull(original.getId());
			
			assertTrue(returned.contains(original));
			
			TestMainObject gotten = this.testMongoService.get(DEFAULT_TEST_DB_NAME, original.getId());
			
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
		
		assertThrows(ValidationException.class, ()->this.testMongoService.addBulk(DEFAULT_TEST_DB_NAME, originals));
		
		assertEquals(0, this.testMongoService.count(DEFAULT_TEST_DB_NAME));
	}
	
	@Test
	public void testAddBulkError2() {
		List<TestMainObject> originals = new ArrayList<>();
		
		originals.add(new TestMainObject());
		for (int i = 1; i <= 5; i++) {
			originals.add(new TestMainObject("Hello world " + i));
		}
		
		log.info("Originals: {}", originals);
		
		assertThrows(ValidationException.class, ()->this.testMongoService.addBulk(DEFAULT_TEST_DB_NAME, originals));
		
		assertEquals(0, this.testMongoService.count(DEFAULT_TEST_DB_NAME));
	}
	// </editor-fold>
	
	// <editor-fold desc="Get">
	@Test
	public void testGetByObjectId() {
		TestMainObject original = new TestMainObject("hello world");
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		TestMainObject gotten = this.testMongoService.get(DEFAULT_TEST_DB_NAME, original.getId());
		
		assertNotSame(original, gotten);
		assertEquals(original, gotten);
	}
	
	@Test
	public void testGetByString() {
		TestMainObject original = new TestMainObject("hello world");
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		TestMainObject gotten = this.testMongoService.get(DEFAULT_TEST_DB_NAME, original.getId().toHexString());
		
		assertNotSame(original, gotten);
		assertEquals(original, gotten);
	}
	
	@Test
	public void testGetByNullObjectId() {
		TestMainObject original = new TestMainObject("hello world");
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		assertThrows(
			DbNotFoundException.class,
			()->{
				this.testMongoService.get(DEFAULT_TEST_DB_NAME, (ObjectId) null);
			}
		);
	}
	
	@Test
	public void testGetByRandomObjectId() {
		TestMainObject original = new TestMainObject("hello world");
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		assertThrows(
			DbNotFoundException.class,
			()->{
				this.testMongoService.get(DEFAULT_TEST_DB_NAME, ObjectId.get());
			}
		);
	}
	// </editor-fold>
	
	// <editor-fold desc="Update">
	@Test
	public void testUpdate() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.put("testField", "something completely different");
		ObjectNode atts = update.putObject("attributes");
		atts.put("some", "val");
		ArrayNode keywords = update.putArray("keywords");
		keywords.add("something");
		
		TestMainObject result = this.testMongoService.update(DEFAULT_TEST_DB_NAME, null, original.getId(), update);
		
		assertNotEquals(original, result);
		assertEquals("something completely different", result.getTestField());
		assertEquals(result.getAttributes(), Map.of("some", "val"));
		assertEquals(result.getKeywords(), List.of("something"));
	}
	
	@Test
	public void testUpdateWithString() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.put("testField", "something completely different");
		ObjectNode atts = update.putObject("attributes");
		atts.put("some", "val");
		ArrayNode keywords = update.putArray("keywords");
		keywords.add("something");
		
		TestMainObject result = this.testMongoService.update(DEFAULT_TEST_DB_NAME, null, original.getId().toHexString(), update);
		
		assertNotEquals(original, result);
		assertEquals("something completely different", result.getTestField());
		assertEquals(result.getAttributes(), Map.of("some", "val"));
		assertEquals(result.getKeywords(), List.of("something"));
	}
	
	@Test
	public void testUpdateInvalidField() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.put("invalidField", "something");
		
		assertThrows(
			IllegalArgumentException.class,
			()->{
				this.testMongoService.update(DEFAULT_TEST_DB_NAME, null, original.getId(), update);
			}
		);
	}
	
	@Test
	public void testUpdateId() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.put("id", ObjectId.get().toHexString());
		
		assertThrows(
			IllegalArgumentException.class,
			()->{
				this.testMongoService.update(DEFAULT_TEST_DB_NAME, null, original.getId(), update);
			}
		);
	}
	
	@Test
	public void testUpdateBadFieldValue() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.put("testField", "");
		
		assertThrows(
			IllegalArgumentException.class,
			()->{
				this.testMongoService.update(DEFAULT_TEST_DB_NAME, null, original.getId(), update);
			}
		);
	}
	
	@Test
	public void testUpdateHistory() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.putObject("history");
		
		assertThrows(
			IllegalArgumentException.class,
			()->{
				this.testMongoService.update(DEFAULT_TEST_DB_NAME, null, original.getId(), update);
			}
		);
	}
	
	@Test
	public void testUpdateNonexistantObj() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		ObjectNode update = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		update.putObject("history");
		
		assertThrows(
			DbNotFoundException.class,
			()->{
				this.testMongoService.update(DEFAULT_TEST_DB_NAME, null, ObjectId.get(), update);
			}
		);
	}
	// </editor-fold>
	
	// <editor-fold desc="Remove">
	@Test
	public void testRemove() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		TestMainObject result = this.testMongoService.remove(DEFAULT_TEST_DB_NAME, original.getId());
		
		assertEquals(0, this.testMongoService.count(DEFAULT_TEST_DB_NAME));
		assertEquals(original.getId(), result.getId());
		assertEquals(original.getTestField(), result.getTestField());
	}
	
	@Test
	public void testRemoveWithString() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		TestMainObject result = this.testMongoService.remove(DEFAULT_TEST_DB_NAME, original.getId().toHexString());
		
		assertEquals(0, this.testMongoService.count(DEFAULT_TEST_DB_NAME));
		assertEquals(original.getId(), result.getId());
		assertEquals(original.getTestField(), result.getTestField());
	}
	
	@Test
	public void testRemoveNullId() {
		TestMainObject original = new TestMainObject("hello world");
		testMongoService.add(DEFAULT_TEST_DB_NAME, original);
		
		assertThrows(
			DbNotFoundException.class,
			()->{
				this.testMongoService.remove(DEFAULT_TEST_DB_NAME, (ObjectId) null);
			}
		);
		
		assertEquals(1, this.testMongoService.count(DEFAULT_TEST_DB_NAME));
	}
	// </editor-fold>
	
	@Test
	public void testSumIntFieldIntegerEmpty() {
		assertEquals(0L, this.testMongoService.getSumOfIntField(DEFAULT_TEST_DB_NAME, "intValue"));
	}
	
	@Test
	public void testSumIntFieldIntegerSmall() {
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), 5));
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), 5));
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), 5));
		
		assertEquals(15L, this.testMongoService.getSumOfIntField(DEFAULT_TEST_DB_NAME, "intValue"));
	}
	
	@Test
	public void testSumIntFieldIntegerLarge() {
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), Integer.MAX_VALUE));
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), Integer.MAX_VALUE));
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), Integer.MAX_VALUE));
		
		assertEquals(
			(long) Integer.MAX_VALUE * 3L,
			this.testMongoService.getSumOfIntField(DEFAULT_TEST_DB_NAME, "intValue")
		);
	}
	
	@Test
	public void testSumIntFieldLong() {
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), (long) Integer.MAX_VALUE));
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), (long) Integer.MAX_VALUE));
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), (long) Integer.MAX_VALUE));
		
		assertEquals(
			(long) Integer.MAX_VALUE * 3L,
			this.testMongoService.getSumOfIntField(DEFAULT_TEST_DB_NAME, "longValue")
		);
	}
	
	@Test
	public void testSumFloatFieldEmpty() {
		assertEquals(
			0.0,
			this.testMongoService.getSumOfFloatField(DEFAULT_TEST_DB_NAME, "floatValue")
		);
	}
	
	@Test
	public void testSumFloatField() {
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), 5.5));
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), 5.5));
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), 5.5));
		
		assertEquals(
			16.5,
			this.testMongoService.getSumOfFloatField(DEFAULT_TEST_DB_NAME, "floatValue")
		);
	}
	
	@Test
	public void testCo() {
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), 5.5));
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), 5.5));
		this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.name().name(), 5.5));
		
		assertEquals(
			CollectionStats.builder().size(3).build(),
			this.testMongoService.getStats(DEFAULT_TEST_DB_NAME)
		);
	}
	
}