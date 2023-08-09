//package tech.ebp.oqm.baseStation.interfaces.endpoints.user;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.quarkus.test.common.QuarkusTestResource;
//import io.quarkus.test.common.http.TestHTTPEndpoint;
//import io.quarkus.test.junit.QuarkusTest;
//import io.restassured.http.ContentType;
//import io.restassured.response.ValidatableResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
//import tech.ebp.oqm.baseStation.interfaces.endpoints.auth.UserAuth;
//import tech.ebp.oqm.baseStation.testResources.TestRestUtils;
//import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
//import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
//import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
//import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
//import tech.ebp.oqm.baseStation.model.rest.ErrorMessage;
//import tech.ebp.oqm.baseStation.model.rest.auth.TokenCheckResponse;
//import tech.ebp.oqm.baseStation.model.rest.auth.user.UserLoginRequest;
//import tech.ebp.oqm.baseStation.model.rest.auth.user.UserLoginResponse;
//
//import jakarta.inject.Inject;
//import jakarta.ws.rs.core.Response;
//
//import static io.restassured.RestAssured.given;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@Tag("integration")
//@Slf4j
//@QuarkusTest
//@QuarkusTestResource(TestResourceLifecycleManager.class)
//@TestHTTPEndpoint(UserAuth.class)
//class AuthTest extends RunningServerTest {
//
//	@Inject
//	ObjectMapper objectMapper;
//	@Inject
//	TestUserService testUserService;
//
//	@Test
//	public void testBadLoginNoUser() throws JsonProcessingException {
//		User testUser = this.testUserService.getTestUser(false, true);
//
//		UserLoginRequest ulr = new UserLoginRequest("bad", "login", true);
//		ErrorMessage errorMessage = given()
//			.contentType(ContentType.JSON)
//			.body(objectMapper.writeValueAsString(ulr))
//			.when()
//			.post()
//			.then()
//			.statusCode(Response.Status.BAD_REQUEST.getStatusCode())
//			.extract().body().as(ErrorMessage.class);
//
//		log.info("Error Message: {}", errorMessage);
//		assertEquals("User not found.", errorMessage.getDisplayMessage());
//	}
//
//	@Test
//	public void testBadLoginBadPass() throws JsonProcessingException {
//		User testUser = this.testUserService.getTestUser(false, true);
//
//		UserLoginRequest ulr = new UserLoginRequest(testUser.getEmail(), "badPass", true);
//		ErrorMessage errorMessage = given()
//			.contentType(ContentType.JSON)
//			.body(objectMapper.writeValueAsString(ulr))
//			.when()
//			.post()
//			.then()
//			.statusCode(Response.Status.BAD_REQUEST.getStatusCode())
//			.extract().body().as(ErrorMessage.class);
//
//		log.info("Error Message: {}", errorMessage);
//		assertEquals("Invalid Password.", errorMessage.getDisplayMessage());
//	}
//
//	@Test
//	public void testLogin() throws JsonProcessingException {
//		User testUser = this.testUserService.getTestUser(false, true);
//
//		UserLoginRequest ulr = new UserLoginRequest(
//			testUser.getEmail(),
//			testUser.getAttributes().get(TestUserService.TEST_PASSWORD_ATT_KEY),
//			true
//		);
//
//		UserLoginResponse loginResponse = given()
//			.contentType(ContentType.JSON)
//			.body(objectMapper.writeValueAsString(ulr))
//			.when()
//			.post()
//			.then()
//			.statusCode(Response.Status.ACCEPTED.getStatusCode())
//			.extract().body().as(UserLoginResponse.class);
//
//		assertNotNull(loginResponse);
//		assertFalse(loginResponse.getToken().isBlank());
//		assertNotNull(loginResponse.getExpires());
//
//		ValidatableResponse response = TestRestUtils.setupJwtCall(
//			given(),
//			loginResponse.getToken()
//		)
//													.contentType(ContentType.JSON)
//													.when()
//										   .basePath(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/auth")
//													.get("/tokenCheck")
//													.then();
//		log.info("token check response: {}", response.extract().body().asString());
//
//		response.statusCode(Response.Status.OK.getStatusCode());
//		TokenCheckResponse tokenCheckResponse = response.extract().body().as(TokenCheckResponse.class);
//
//		log.info("Token Check Response: {}", tokenCheckResponse);
//	}
//}