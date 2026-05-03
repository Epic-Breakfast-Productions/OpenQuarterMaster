package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import jakarta.inject.Inject;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
					   .body(updates)
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

}