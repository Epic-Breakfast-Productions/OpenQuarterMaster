//package tech.ebp.oqm.baseStation.interfaces.endpoints.user;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.quarkus.test.common.QuarkusTestResource;
//import io.quarkus.test.common.ResourceArg;
//import io.quarkus.test.common.http.TestHTTPEndpoint;
//import io.quarkus.test.junit.QuarkusTest;
//import io.quarkus.test.junit.TestProfile;
//import io.restassured.http.ContentType;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
//import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
//import tech.ebp.oqm.baseStation.testResources.profiles.ExternalAuthTestProfile;
//import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
//import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
//import tech.ebp.oqm.baseStation.model.rest.user.UserCreateRequest;
//
//import jakarta.inject.Inject;
//import jakarta.ws.rs.core.Response;
//
//import static io.restassured.RestAssured.given;
//
//@Tag("integration")
//@Tag("externalAuth")
//@Slf4j
//@QuarkusTest
//@TestProfile(ExternalAuthTestProfile.class)
//@QuarkusTestResource(
//	value = TestResourceLifecycleManager.class,
//	initArgs = @ResourceArg(name = TestResourceLifecycleManager.EXTERNAL_AUTH_ARG, value = "true"),
//	restrictToAnnotatedClass = true
//)
//@TestHTTPEndpoint(UserCrud.class)
//class UserCrudExternalTest extends RunningServerTest {
//
//	@Inject
//	ObjectMapper objectMapper;
//	@Inject
//	TestUserService testUserService;
//
//	@Test
//	public void testCreateUserAttempt() throws JsonProcessingException {
//		User testUser = this.testUserService.getTestUser(false, false);
//
//		UserCreateRequest ucr = new UserCreateRequest(
//			testUser.getFirstName(),
//			testUser.getLastName(),
//			testUser.getUsername(),
//			testUser.getEmail(),
//			testUser.getTitle(),
//			"1!Letmein",
//			testUser.getAttributes()
//		);
//
//		String errorMessage = given()
//			.contentType(ContentType.JSON)
//			.body(objectMapper.writeValueAsString(ucr))
//			.when()
//			.post()
//			.then()
//			.statusCode(Response.Status.FORBIDDEN.getStatusCode())
//			.extract().body().toString();
//
//		log.info("Error Message: {}", errorMessage);
//		//        assertEquals("User not found.", errorMessage.getError());
//	}
//
//}