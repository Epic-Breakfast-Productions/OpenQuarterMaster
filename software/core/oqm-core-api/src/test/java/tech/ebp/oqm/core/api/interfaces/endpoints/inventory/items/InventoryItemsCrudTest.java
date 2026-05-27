package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.serviceState.InstanceMutexService;
import tech.ebp.oqm.core.api.service.serviceState.InstanceMutexServiceTest;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import jakarta.inject.Inject;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Tag("integration")
@Slf4j
@QuarkusTest
@TestHTTPEndpoint(InventoryItemsCrud.class)
class InventoryItemsCrudTest extends RunningServerTest {
	
	@Inject
	InventoryItemTestObjectCreator testObjectCreator;
	
	@Inject
	ObjectMapper objectMapper;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	
	@Test
	public void testItemSearchId() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();
		
		String json = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
						  .body(objectMapper.writeValueAsString(testObjectCreator.getTestObject()))
						  .contentType(ContentType.JSON)
						  .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
						  .post()
						  .then().statusCode(200)
						  .extract().body().asString();
		
		String id = OBJECT_MAPPER.readValue(json, InventoryItem.class).getId().toHexString();
		
		String resultStr = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
							   .body(objectMapper.writeValueAsString(testObjectCreator.getTestObject()))
							   .contentType(ContentType.JSON)
							   .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
							   .params(Map.of("id", id))
							   .get()
							   .then().statusCode(200)
							   .extract().body().asString();
		
		ObjectNode resultNode = (ObjectNode) OBJECT_MAPPER.readTree(resultStr);
		
		assertEquals(1, resultNode.get("numResults").asInt());
		
		resultStr = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
						.body(objectMapper.writeValueAsString(testObjectCreator.getTestObject()))
						.contentType(ContentType.JSON)
						.pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
						.params(Map.of("id", new ObjectId().toHexString()))
						.get()
						.then().statusCode(200)
						.extract().body().asString();
		
		resultNode = (ObjectNode) OBJECT_MAPPER.readTree(resultStr);
		
		assertEquals(0, resultNode.get("numResults").asInt());
	}
	
	@Test
	public void testItemUpdatesUnit() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();
		
		String json = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
						  .body(objectMapper.writeValueAsString(testObjectCreator.getTestObject()))
						  .contentType(ContentType.JSON)
						  .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
						  .post()
						  .then().statusCode(200)
						  .extract().body().asString();
		
		log.info("Initial item: {}", json);
		
		String id = OBJECT_MAPPER.readValue(json, InventoryItem.class).getId().toHexString();
		
		ObjectNode updates = OBJECT_MAPPER.createObjectNode();
		updates.putObject("unit")
			.put("string", "mol");
		
		//initial update
		ValidatableResponse response = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
										   .when()
										   .body(updates)
										   .contentType(ContentType.JSON)
										   .accept(ContentType.JSON)
										   .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
										   .put(id)
										   .then()
										   .statusCode(200);
		
		ObjectNode result = response.extract().as(ObjectNode.class);
		
		log.info("Update Result: {}", result);
		
		assertEquals("mol", result.get("unit").get("string").asText());
		assertEquals("mol", result.get("stats").get("total").get("unit").get("string").asText());
		
		//get again
		response = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
					   .when()
					   .accept(ContentType.JSON)
					   .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
					   .get(id)
					   .then()
					   .statusCode(200);
		
		result = response.extract().as(ObjectNode.class);
		
		log.info("Get Result: {}", result);
		
		assertEquals("mol", result.get("unit").get("string").asText());
		assertEquals("mol", result.get("stats").get("total").get("unit").get("string").asText());
	}
	
	public static Stream<Arguments> getParams() {
		return Stream.of(
			Arguments.of(2, 10),
			Arguments.of(20, 20)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getParams")
	public void basicThreadTest(int numThreads, int numIterations) throws InterruptedException, ExecutionException, JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();
		String json = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
						  .body(objectMapper.writeValueAsString(testObjectCreator.getTestObject()))
						  .contentType(ContentType.JSON)
						  .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
						  .post()
						  .then().statusCode(200)
						  .extract().body().asString();
		
		log.info("Initial item: {}", json);
		
		String id = OBJECT_MAPPER.readValue(json, InventoryItem.class).getId().toHexString();
		List<Future<Void>> futures = new ArrayList<>(numThreads);
		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
		
		
		BasicItemUpdateTestThread.BasicItemUpdateTestThreadBuilder threadBuilder = BasicItemUpdateTestThread.builder()
																					   .testUser(testUser)
																					   .inventoryItemId(id)
																					   .numIterations(numIterations);
		
		StopWatch overall = StopWatch.createStarted();
		for (int i = 1; i <= numThreads; i++) {
			threadBuilder.threadId("testThread-" + i);
			
			futures.add(executor.submit(threadBuilder.build()));
		}
		executor.shutdown();
		while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
			log.info("Still waiting on threads...");
		}
		overall.stop();
		
		for (Future<Void> future : futures) {
			future.get();
		}
		
		//TODO:: check results
		ObjectNode result = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
								.when()
								.accept(ContentType.JSON)
								.pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
								.get(id)
								.then()
								.statusCode(200)
								.extract().as(ObjectNode.class);
		log.info("Got Result: {}", result);
		
	}
	
	
	@Builder
	@Slf4j
	@AllArgsConstructor
	static class BasicItemUpdateTestThread implements Callable<Void> {
		
		private String threadId;
		private String inventoryItemId;
		private User testUser;
		
		private int numIterations;
		
		@SneakyThrows
		@Override
		public Void call() {
			log.info("Running test thread {}", this.threadId);
			
				
				ObjectNode updates = OBJECT_MAPPER.createObjectNode();
				updates.putObject("attributes");
				
				for (int i = 1; i <= this.numIterations; i++) {
					log.info("Thread {} waiting for lock on iteration {}", this.threadId, i);
					
					((ObjectNode) updates.get("attributes")).put(this.threadId, i);
					
					
					try {
						//initial update
						ValidatableResponse response = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
														   .when()
														   .body(updates)
														   .contentType(ContentType.JSON)
														   .accept(ContentType.JSON)
														   .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
														   .put(this.inventoryItemId)
														   .then()
														   .statusCode(200);
					} catch (Throwable e) {
						if(e instanceof SocketTimeoutException) {//somehow not able to be in its own catch
							//not an issue, typically... only seems to happen in extreme cases.
							log.warn("Socket timeout on iteration {}", i);
							i--;
							continue;
						}
						
						log.error("Error in thread {} on iteration {}", this.threadId, i, e);
						throw new RuntimeException("Error thrown in thread " + this.threadId + " on iteration " + i, e);
					}
				}
			
			log.info("DONE running test thread {}", this.threadId);
			return null;
		}
	}
	
}