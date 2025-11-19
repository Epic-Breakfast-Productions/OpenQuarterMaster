package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.rest.ErrorMessage;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Tag("integration")
@Slf4j
@QuarkusTest
@TestHTTPEndpoint(StorageCrud.class)
class StorageCrudTest extends RunningServerTest {
	
	@Inject
	ObjectMapper objectMapper;
	
	public static Stream<Arguments> getBadStorageArgs() {
		return Stream.of(
			Arguments.of(""),
			Arguments.of("{"),
			Arguments.of(ObjectUtils.OBJECT_MAPPER.createObjectNode().toPrettyString())
		);
	}
	
	
	
	@Test
	public void testCreate() throws JsonProcessingException {
		User user = this.getTestUserService().getTestUser();
		
		ObjectNode newBlockData = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		
		newBlockData.put("label", "test block");
		
		String returnStr = setupJwtCall(given(), user.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
							   .contentType(ContentType.JSON)
							   .body(objectMapper.writeValueAsString(newBlockData))
							   .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
							   .when()
							   .post()
							   .then()
							   .statusCode(Response.Status.OK.getStatusCode())
							   .contentType(ContentType.JSON)
							   .extract().body().asString();
		
		ObjectNode returnNode = (ObjectNode) objectMapper.readTree(returnStr);
		
		assertEquals(newBlockData.get("label").asText(), returnNode.get("label").asText());
	}
	
	@Test
	public void testBadCreds() throws JsonProcessingException {
		User user = this.getTestUserService().getTestUser();
		
		ObjectNode newBlockData = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		newBlockData.put("label", "test block");
		
		ValidatableResponse response = setupJwtCall(given(), user.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY) + "foo")
										   .contentType(ContentType.JSON)
										   .body(newBlockData)
										   .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
										   .when()
										   .post()
										   .then();
		
		String returnStr = response.extract().body().asString();
		
		log.info("Return: {}", returnStr);
		
		response.statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
			.contentType(ContentType.JSON);
		
		ErrorMessage errorMessage = objectMapper.readValue(returnStr, ErrorMessage.class);
		log.info("Error message: {}", errorMessage);
	}
	
	@ParameterizedTest
	@MethodSource("getBadStorageArgs")
	public void testBadCreate(String createJson) throws IOException {
		User user = this.getTestUserService().getTestUser();
		
		
		ValidatableResponse response = setupJwtCall(given(), user.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
							   .contentType(ContentType.JSON)
							   .body(createJson)
							   .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
							   .when()
							   .post()
							   .then();
		
		String returnStr = response.extract().body().asString();
		
		log.info("Return: {}", returnStr);
		
		response.statusCode(Response.Status.BAD_REQUEST.getStatusCode())
			 .contentType(ContentType.JSON);
		
		ErrorMessage errorMessage = objectMapper.readValue(returnStr, ErrorMessage.class);
		log.info("Error message: {}", errorMessage);
	}
}