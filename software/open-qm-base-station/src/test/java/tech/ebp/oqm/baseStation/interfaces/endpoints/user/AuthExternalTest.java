package tech.ebp.oqm.baseStation.interfaces.endpoints.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.interfaces.endpoints.auth.GeneralAuth;
import tech.ebp.oqm.baseStation.interfaces.endpoints.auth.UserAuth;
import tech.ebp.oqm.baseStation.testResources.TestRestUtils;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.profiles.ExternalAuthTestProfile;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.rest.auth.TokenCheckResponse;
import tech.ebp.oqm.baseStation.model.rest.auth.user.UserLoginRequest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;

@Tag("integration")
@Tag("externalAuth")
@Slf4j
@QuarkusTest
@TestProfile(ExternalAuthTestProfile.class)
@QuarkusTestResource(
	value = TestResourceLifecycleManager.class,
	initArgs = @ResourceArg(name = TestResourceLifecycleManager.EXTERNAL_AUTH_ARG, value = "true"),
	restrictToAnnotatedClass = true
)
class AuthExternalTest extends RunningServerTest {
	
	@Inject
	ObjectMapper objectMapper;
	@Inject
	TestUserService testUserService;
	
	@ConfigProperty(name = "service.externalAuth.realmBase")
	String base;
	@ConfigProperty(name = "service.externalAuth.realmBasePath")
	String basePath;
	@ConfigProperty(name = "service.externalAuth.url")
	String url;
	
	@Test
	@TestHTTPEndpoint(UserAuth.class)
	public void testLoginEndpoint() throws JsonProcessingException {
		User testUser = this.testUserService.getTestUser(false, true);
		
		UserLoginRequest ulr = new UserLoginRequest(
			testUser.getEmail(),
			testUser.getAttributes().get(TestUserService.TEST_PASSWORD_ATT_KEY),
			true
		);
		
		String errorMessage = given()
			.contentType(ContentType.JSON)
			.body(objectMapper.writeValueAsString(ulr))
			.when()
			.post()
			.then()
			.statusCode(Response.Status.FORBIDDEN.getStatusCode())
			.extract().body().toString();
		
		log.info("Error Message: {}", errorMessage);
	}
	
	@Test
	@TestHTTPEndpoint(GeneralAuth.class)
	public void testTokenCheck() throws JsonProcessingException {
		User testUser = this.testUserService.getTestUser(false, true);
		
		String token = this.testUserService.getTestUserToken(testUser);
		
		log.info("Token from external: {}", token);
		
		ValidatableResponse response = TestRestUtils.setupJwtCall(
			given(),
			token
		)
													.contentType(ContentType.JSON)
													.when()
													.get("/tokenCheck")
													.then();
		
		log.info("token check response: {}", response.extract().body().asString());
		
		response.statusCode(Response.Status.OK.getStatusCode());
		TokenCheckResponse tokenCheckResponse = response.extract().body().as(TokenCheckResponse.class);
		
		log.info("Token Check Response: {}", tokenCheckResponse);
	}
}