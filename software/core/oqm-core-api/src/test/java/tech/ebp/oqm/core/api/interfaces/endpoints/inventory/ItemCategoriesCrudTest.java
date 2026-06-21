package tech.ebp.oqm.core.api.interfaces.endpoints.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.rest.ErrorMessage;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Tag("integration")
@QuarkusTest
@TestHTTPEndpoint(ItemCategoriesCrud.class)
class ItemCategoriesCrudTest extends RunningServerTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void createTest() throws JsonProcessingException {
        User testUser = this.getTestUserService().getTestUser();

        ItemCategory result = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .body(objectMapper.writeValueAsString(new ItemCategory().setName("Test Category").setDescription("A category created during testing.")))
            .contentType(ContentType.JSON)
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .post()
            .then().statusCode(200)
            .extract().response().as(ItemCategory.class);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Category", result.getName());
        assertEquals("A category created during testing.", result.getDescription());

        ItemCategory dbResult = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .pathParam("id", result.getId().toHexString())
            .get("/{id}")
            .then().statusCode(200)
            .extract().response().as(ItemCategory.class);

        assertEquals("Test Category", dbResult.getName());
    }

    @Test
    void createInvalidTest() throws JsonProcessingException {
        User testUser = this.getTestUserService().getTestUser();

        ErrorMessage errorMessage = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .body(objectMapper.writeValueAsString(new ItemCategory().setName("")))
            .contentType(ContentType.JSON)
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .post()
            .then().statusCode(400)
            .extract().response().as(ErrorMessage.class);

        assertNotNull(errorMessage.getDisplayMessage(), "Error message should have a display message");
        assertTrue(errorMessage.getDisplayMessage().contains("name"), "Error message should contain 'name'");
    }

    @Test
    void updateTest() throws JsonProcessingException {
        User testUser = this.getTestUserService().getTestUser();

        ItemCategory created = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .body(objectMapper.writeValueAsString(new ItemCategory().setName("To Update").setDescription("Initial")))
            .contentType(ContentType.JSON)
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .post()
            .then().statusCode(200)
            .extract().response().as(ItemCategory.class);

        ItemCategory updated = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .body("{\"name\": \"Updated Name\"}")
            .contentType(ContentType.JSON)
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .pathParam("id", created.getId().toHexString())
            .put("/{id}")
            .then().statusCode(200)
            .extract().response().as(ItemCategory.class);

        assertEquals("Updated Name", updated.getName());
        assertEquals("Initial", updated.getDescription());
    }

    @Test
    void updateNotFoundTest() {
        User testUser = this.getTestUserService().getTestUser();

        ErrorMessage errorMessage = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .body("{\"name\": \"Updated Name\"}")
            .contentType(ContentType.JSON)
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .pathParam("id", new ObjectId().toHexString())
            .put("/{id}")
            .then().statusCode(404)
            .extract().response().as(ErrorMessage.class);

        assertNotNull(errorMessage.getDisplayMessage());
        assertTrue(errorMessage.getDisplayMessage().contains("Could not find history for ItemCategory with id"), "Error message should indicate not found");
        assertTrue(errorMessage.getDisplayMessage().contains("ItemCategory"), "Error message should indicate ItemCategory");
    }

    @Test
    void deleteTest() throws JsonProcessingException {
        User testUser = this.getTestUserService().getTestUser();

        ItemCategory created = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .body(objectMapper.writeValueAsString(new ItemCategory().setName("To Delete")))
            .contentType(ContentType.JSON)
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .post()
            .then().statusCode(200)
            .extract().response().as(ItemCategory.class);

        setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .pathParam("id", created.getId().toHexString())
            .delete("/{id}")
            .then().statusCode(200);

        setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .pathParam("id", created.getId().toHexString())
            .get("/{id}")
            .then().statusCode(404);
    }

    @Test
    void deleteNotFoundTest() {
        User testUser = this.getTestUserService().getTestUser();

        ErrorMessage errorMessage = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .pathParam("id", new org.bson.types.ObjectId().toHexString())
            .delete("/{id}")
            .then().statusCode(404)
            .extract().response().as(ErrorMessage.class);

        assertNotNull(errorMessage.getDisplayMessage());
        assertTrue(errorMessage.getDisplayMessage().contains("Could not find ItemCategory with id"), "Error message should indicate not found");
    }

    @Test
    void getNotFoundTest() {
        User testUser = this.getTestUserService().getTestUser();

        ErrorMessage errorMessage = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .pathParam("id", new org.bson.types.ObjectId().toHexString())
            .get("/{id}")
            .then().statusCode(404)
            .extract().response().as(ErrorMessage.class);
        assertNotNull(errorMessage.getDisplayMessage());
        assertTrue(errorMessage.getDisplayMessage().contains("Could not find history for ItemCategory with id"), "Error message should indicate not found");
    }
}
