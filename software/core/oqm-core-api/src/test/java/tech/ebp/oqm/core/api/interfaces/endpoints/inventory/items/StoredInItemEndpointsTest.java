package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import static io.restassured.RestAssured.given;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Tag("integration")
@Slf4j
@QuarkusTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class)
//@TestHTTPEndpoint(StoredEndpoints.class)
public class StoredInItemEndpointsTest extends RunningServerTest {

	@Inject
	InventoryItemTestObjectCreator testObjectCreator;

	@Inject
	ObjectMapper objectMapper;
	
	@Test
	public void testSearchEmptyDb() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();
		
		String json = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
						  .body(objectMapper.writeValueAsString(testObjectCreator.getTestObject()))
						  .contentType(ContentType.JSON)
						  .post("/api/v1/db/"+DEFAULT_TEST_DB_NAME+"/inventory/item")
						  .then().statusCode(200)
						  .extract().body().asString();
		
		String id = OBJECT_MAPPER.readValue(json, InventoryItem.class).getId().toHexString();
		
		ValidatableResponse response = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
										   .when()
										   .get("/api/v1/db/"+DEFAULT_TEST_DB_NAME+"/inventory/item/"+id+"/stored")
										   .then()
										   .statusCode(200);
		
		log.info("Search result: {}", response.extract().asString());
	}
	
}
