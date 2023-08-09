//package tech.ebp.oqm.baseStation.interfaces.endpoints.externalService;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.quarkus.test.common.QuarkusTestResource;
//import io.quarkus.test.common.http.TestHTTPEndpoint;
//import io.quarkus.test.junit.QuarkusTest;
//import io.restassured.http.ContentType;
//import io.restassured.response.ValidatableResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.jboss.resteasy.reactive.RestResponse;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import tech.ebp.oqm.baseStation.config.ExtServicesConfig;
//import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
//import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.ExternalService;
//import tech.ebp.oqm.baseStation.model.rest.auth.TokenCheckResponse;
//import tech.ebp.oqm.baseStation.model.rest.auth.externalService.ExternalServiceLoginRequest;
//import tech.ebp.oqm.baseStation.model.rest.auth.externalService.ExternalServiceLoginResponse;
//import tech.ebp.oqm.baseStation.model.rest.externalService.ExternalServiceSetupRequest;
//import tech.ebp.oqm.baseStation.model.rest.externalService.ExternalServiceSetupResponse;
//import tech.ebp.oqm.baseStation.model.rest.externalService.GeneralServiceSetupRequest;
//import tech.ebp.oqm.baseStation.service.mongo.ExternalServiceService;
//import tech.ebp.oqm.baseStation.testResources.TestRestUtils;
//import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
//import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
//
//import jakarta.inject.Inject;
//import jakarta.ws.rs.core.Response;
//import java.util.stream.Stream;
//
//import static io.restassured.RestAssured.given;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@Tag("integration")
//@Slf4j
//@QuarkusTest
//@QuarkusTestResource(TestResourceLifecycleManager.class)
//@TestHTTPEndpoint(ExternalServiceEp.class)
//class ExternalServiceTest extends RunningServerTest {
//
//	//TODO:: make TestExternalServiceService
//	@Inject
//	ExternalServiceService externalServiceService;
//
//	@Inject
//	ExtServicesConfig extServicesConfig;
//
//	@Inject
//	ObjectMapper objectMapper;
//
//	public static Stream<Arguments> getValidExtServiceSetupRequests(){
//		return Stream.of(
//			Arguments.of(GeneralServiceSetupRequest.builder()
//							 .name("testService")
//							 .description(FAKER.lorem().paragraph())
//							 .developerEmail(FAKER.internet().emailAddress())
//							 .developerName(FAKER.name().fullName())
//												   .build())
//		);
//	}
//
//
//	@ParameterizedTest
//	@MethodSource("getValidExtServiceSetupRequests")
//	public void testSetupExtService(ExternalServiceSetupRequest setupRequest) {
//		log.info("Testing setting up service {}", setupRequest);
//
//		setupRequest.setSecret(
//			this.extServicesConfig.extServices()
//								  .get(setupRequest.getName())
//								  .secret()
//		);
//
//		ValidatableResponse response = given()
//			.contentType(ContentType.JSON)
//			.body(setupRequest)
//			.when()
//			.post("setup/self")
//			.then();
//
//		response.statusCode(RestResponse.StatusCode.OK);
//		ExternalServiceSetupResponse setupResponse = response.extract().body().as(ExternalServiceSetupResponse.class);
//
//		assertNotNull(setupResponse.getId());
//		assertNotNull(setupResponse.getSetupToken());
//		assertNotNull(setupResponse.getGrantedRoles());
//
//		ExternalService service = this.externalServiceService.get(setupResponse.getId());
//
//		assertEquals(setupRequest.getServiceType(), service.getServiceType());
//		assertEquals(setupRequest.getName(), service.getName());
//		assertEquals(setupRequest.getDescription(), service.getDescription());
//		assertEquals(setupRequest.getDeveloperName(), service.getDeveloperName());
//		assertEquals(setupRequest.getDeveloperEmail(), service.getDeveloperEmail());
//
//		//TODO:: test roles, service-type-specific fields
//	}
//
//	//TODO:: test bad service name
//	//TODO:: test bad service secret
//
//	@ParameterizedTest
//	@MethodSource("getValidExtServiceSetupRequests")
//	public void testAuthExtService(ExternalServiceSetupRequest setupRequest) throws JsonProcessingException {
//		log.info("Testing setting up service {}", setupRequest);
//
//		setupRequest.setSecret(
//			this.extServicesConfig.extServices()
//								  .get(setupRequest.getName())
//								  .secret()
//		);
//		ExternalServiceSetupResponse setupResponse  = given()
//										   .contentType(ContentType.JSON)
//										   .body(setupRequest)
//										   .when()
//										   .post("setup/self")
//										   .then().statusCode(RestResponse.StatusCode.OK)
//										   .extract().body().as(ExternalServiceSetupResponse.class);
//
//		log.info("Setup Response: {}", setupResponse);
//
//		ExternalServiceLoginRequest loginRequest = setupResponse.toLoginRequest();
//		ValidatableResponse response =  given()
//			.contentType(ContentType.JSON)
//			.body(objectMapper.writeValueAsString(loginRequest))
//			.when()
//			.post("auth")
//			.then();
//
//		ExternalServiceLoginResponse authResponse = response
//			.statusCode(RestResponse.StatusCode.OK)
//			.extract().body().as(ExternalServiceLoginResponse.class);
//
//		assertNotNull(authResponse.getToken());
//
//		ValidatableResponse tokenCheckResponse = TestRestUtils.setupJwtCall(
//														given(),
//														authResponse.getToken()
//													)
//													.contentType(ContentType.JSON)
//													.when()
//													.basePath(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/auth")
//													.get("/tokenCheck")
//													.then();
//		log.info("token check response: {}", tokenCheckResponse.extract().body().asString());
//
//		tokenCheckResponse.statusCode(Response.Status.OK.getStatusCode());
//		TokenCheckResponse tokenCheckResponseObj = tokenCheckResponse.extract().body().as(TokenCheckResponse.class);
//
//		log.info("Token Check Response: {}", tokenCheckResponseObj);
//	}
//
//	//TODO:: test auth when authModeExt (different test class)
//	//TODO:: test bad service token
//}