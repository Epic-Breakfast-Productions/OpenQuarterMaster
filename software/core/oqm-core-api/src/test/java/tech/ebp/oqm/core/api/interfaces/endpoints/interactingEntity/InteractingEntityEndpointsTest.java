package tech.ebp.oqm.core.api.interfaces.endpoints.interactingEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.interfaces.endpoints.inventory.ItemCategoriesCrud;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.GeneralService;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;
import static tech.ebp.oqm.core.api.testResources.data.TestUserService.TEST_JWT_ATT_KEY;

@Slf4j
@Tag("integration")
@QuarkusTest
@TestHTTPEndpoint(InteractingEntityEndpoints.class)
class InteractingEntityEndpointsTest extends RunningServerTest {

	@Test
	public void testUserGetSelf() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();

		String result = setupJwtCall(given(), testUser.getAttributes().get(TEST_JWT_ATT_KEY))
			.contentType(ContentType.JSON)
			.get("self")
			.then().statusCode(200)
			.extract().body().asString();


		User gotten = OBJECT_MAPPER.readValue(result, User.class);


		log.info(" Original User info: {}", testUser);
		log.info("Resulting User info: {}", gotten);

		assertEquals(
			testUser.toBuilder()
						 .attributes(Map.of())
						 .build(),
			gotten
		);
	}


	@Test
	public void testUserGetSelfNullEmail() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser(true, false);

		testUser.setEmail(null);
		testUser.getAttributes().put(TEST_JWT_ATT_KEY, this.getTestUserService().getUserToken(testUser));


		String result = setupJwtCall(given(), testUser.getAttributes().get(TEST_JWT_ATT_KEY))
							.contentType(ContentType.JSON)
							.get("self")
							.then().statusCode(200)
							.extract().body().asString();


		User gotten = OBJECT_MAPPER.readValue(result, User.class);


		log.info(" Original User info: {}", testUser);
		log.info("Resulting User info: {}", gotten);

		assertNotNull(gotten.getId());

		assertEquals(
			testUser.toBuilder()
				.attributes(Map.of())
				.id(gotten.getId())
				.build(),
			gotten
		);
	}

	@Test
	public void testUserGetSelfNullUserName() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser(true, false);

		testUser.setUsername(null);
		testUser.getAttributes().put(TEST_JWT_ATT_KEY, this.getTestUserService().getUserToken(testUser));


		String result = setupJwtCall(given(), testUser.getAttributes().get(TEST_JWT_ATT_KEY))
							.contentType(ContentType.JSON)
							.get("self")
							.then().statusCode(200)
							.extract().body().asString();


		User gotten = OBJECT_MAPPER.readValue(result, User.class);


		log.info(" Original User info: {}", testUser);
		log.info("Resulting User info: {}", gotten);

		assertNotNull(gotten.getId());

		assertEquals(
			testUser.toBuilder()
				.attributes(Map.of())
				.id(gotten.getId())
				.build(),
			gotten
		);
	}

	@Test
	public void testUserGetSelfNullEmailAndUserName() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser(true, false);

		testUser.setEmail(null);
		testUser.setUsername(null);
		testUser.getAttributes().put(TEST_JWT_ATT_KEY, this.getTestUserService().getUserToken(testUser));

		String result = setupJwtCall(given(), testUser.getAttributes().get(TEST_JWT_ATT_KEY))
							.contentType(ContentType.JSON)
							.get("self")
							.then().statusCode(200)
							.extract().body().asString();


		User gotten = OBJECT_MAPPER.readValue(result, User.class);


		log.info(" Original User info: {}", testUser);
		log.info("Resulting User info: {}", gotten);

		assertNotNull(gotten.getId());

		assertEquals(
			testUser.toBuilder()
				.attributes(Map.of())
				.id(gotten.getId())
				.build(),
			gotten
		);
	}


	@Test
	public void testServiceGetSelf() throws JsonProcessingException {
		GeneralService testUser = this.getTestUserService().getServiceAccount(false);

		log.info("Service account token: {}", testUser.getAttributes().get(TEST_JWT_ATT_KEY));

		String result = setupJwtCall(given(), testUser.getAttributes().get(TEST_JWT_ATT_KEY))
							.contentType(ContentType.JSON)
							.get("self")
							.then().statusCode(200)
							.extract().body().asString();


		GeneralService gotten = OBJECT_MAPPER.readValue(result, GeneralService.class);


		log.info(" Original User info: {}", testUser);
		log.info("Resulting User info: {}", gotten);

		assertNotNull(gotten.getId());

		assertEquals(
			testUser.toBuilder()
				.attributes(Map.of())
				.id(gotten.getId())
				.build(),
			gotten
		);
	}

	@Test
	public void testServiceGetSelfNullDevName() throws JsonProcessingException {
		GeneralService testUser = this.getTestUserService().getServiceAccount(false);

		testUser.setDeveloperName(null);
		testUser.getAttributes().put(TEST_JWT_ATT_KEY, this.getTestUserService().getServiceToken(testUser));

		log.info("Service account token: {}", testUser.getAttributes().get(TEST_JWT_ATT_KEY));

		String result = setupJwtCall(given(), testUser.getAttributes().get(TEST_JWT_ATT_KEY))
							.contentType(ContentType.JSON)
							.get("self")
							.then().statusCode(200)
							.extract().body().asString();


		GeneralService gotten = OBJECT_MAPPER.readValue(result, GeneralService.class);


		log.info(" Original User info: {}", testUser);
		log.info("Resulting User info: {}", gotten);

		assertNotNull(gotten.getId());

		assertEquals(
			testUser.toBuilder()
				.attributes(Map.of())
				.id(gotten.getId())
				.build(),
			gotten
		);
	}

	@Test
	public void testServiceGetSelfNullDevEmail() throws JsonProcessingException {
		GeneralService testUser = this.getTestUserService().getServiceAccount(false);

		testUser.setDeveloperEmail(null);
		testUser.getAttributes().put(TEST_JWT_ATT_KEY, this.getTestUserService().getServiceToken(testUser));

		log.info("Service account token: {}", testUser.getAttributes().get(TEST_JWT_ATT_KEY));

		String result = setupJwtCall(given(), testUser.getAttributes().get(TEST_JWT_ATT_KEY))
							.contentType(ContentType.JSON)
							.get("self")
							.then().statusCode(200)
							.extract().body().asString();


		GeneralService gotten = OBJECT_MAPPER.readValue(result, GeneralService.class);


		log.info(" Original User info: {}", testUser);
		log.info("Resulting User info: {}", gotten);

		assertNotNull(gotten.getId());

		assertEquals(
			testUser.toBuilder()
				.attributes(Map.of())
				.id(gotten.getId())
				.build(),
			gotten
		);
	}

	@Test
	public void testServiceGetSelfNullDevWebsite() throws JsonProcessingException {
		GeneralService testUser = this.getTestUserService().getServiceAccount(false);

		testUser.setDeveloperWebsite(null);
		testUser.getAttributes().put(TEST_JWT_ATT_KEY, this.getTestUserService().getServiceToken(testUser));

		log.info("Service account token: {}", testUser.getAttributes().get(TEST_JWT_ATT_KEY));

		String result = setupJwtCall(given(), testUser.getAttributes().get(TEST_JWT_ATT_KEY))
							.contentType(ContentType.JSON)
							.get("self")
							.then().statusCode(200)
							.extract().body().asString();


		GeneralService gotten = OBJECT_MAPPER.readValue(result, GeneralService.class);


		log.info(" Original User info: {}", testUser);
		log.info("Resulting User info: {}", gotten);

		assertNotNull(gotten.getId());

		assertEquals(
			testUser.toBuilder()
				.attributes(Map.of())
				.id(gotten.getId())
				.build(),
			gotten
		);
	}

	@Test
	public void testServiceGetSelfNullDevDetails() throws JsonProcessingException {
		GeneralService testUser = this.getTestUserService().getServiceAccount(false);

		testUser.setDeveloperName(null);
		testUser.setDeveloperWebsite(null);
		testUser.setDeveloperEmail(null);
		testUser.getAttributes().put(TEST_JWT_ATT_KEY, this.getTestUserService().getServiceToken(testUser));

		log.info("Service account token: {}", testUser.getAttributes().get(TEST_JWT_ATT_KEY));

		String result = setupJwtCall(given(), testUser.getAttributes().get(TEST_JWT_ATT_KEY))
							.contentType(ContentType.JSON)
							.get("self")
							.then().statusCode(200)
							.extract().body().asString();


		GeneralService gotten = OBJECT_MAPPER.readValue(result, GeneralService.class);


		log.info(" Original User info: {}", testUser);
		log.info("Resulting User info: {}", gotten);

		assertNotNull(gotten.getId());

		assertEquals(
			testUser.toBuilder()
				.attributes(Map.of())
				.id(gotten.getId())
				.build(),
			gotten
		);
	}

}
