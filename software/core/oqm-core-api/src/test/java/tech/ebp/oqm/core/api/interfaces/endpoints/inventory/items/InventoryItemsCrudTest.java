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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Tag("integration")
@Slf4j
@QuarkusTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class)
class InventoryItemsCrudTest extends RunningServerTest {
	
	@Inject
	InventoryItemTestObjectCreator testObjectCreator;
	
	@Inject
	ObjectMapper objectMapper;
	
	@Inject
	InventoryItemService inventoryItemService;


	

	//TODO:: this
	
	
	@Test
	public void testItemUpdatesUnit() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();
		
		String json = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
						  .body(objectMapper.writeValueAsString(testObjectCreator.getTestObject()))
						  .contentType(ContentType.JSON)
						  .post("/api/v1/db/"+DEFAULT_TEST_DB_NAME+"/inventory/item")
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
										   .put("/api/v1/db/"+DEFAULT_TEST_DB_NAME+"/inventory/item/"+id)
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
					   .get("/api/v1/db/"+DEFAULT_TEST_DB_NAME+"/inventory/item/"+id)
					   .then()
					   .statusCode(200);
		
		result = response.extract().as(ObjectNode.class);
		
		log.info("Get Result: {}", result);
		
		assertEquals("mol", result.get("unit").get("string").asText());
		assertEquals("mol", result.get("stats").get("total").get("unit").get("string").asText());
	}

}