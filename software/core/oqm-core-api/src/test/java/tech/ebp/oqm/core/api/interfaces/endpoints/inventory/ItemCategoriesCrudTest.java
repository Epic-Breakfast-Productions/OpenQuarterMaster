package tech.ebp.oqm.core.api.interfaces.endpoints.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import static io.restassured.RestAssured.given;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Slf4j
@Tag("integration")
@QuarkusTest
@TestHTTPEndpoint(ItemCategoriesCrud.class)
class ItemCategoriesCrudTest extends RunningServerTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void createTest() throws JsonProcessingException {
        User testUser = this.getTestUserService().getTestUser();

        setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .body(objectMapper.writeValueAsString(new ItemCategory().setName("Test Category").setDescription("A category created during testing.")))
            .contentType(ContentType.JSON)
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .post()
            .then().statusCode(200)
            .extract().response().asString();
    }

    @Test
    void createInvalidTest() throws JsonProcessingException {
        User testUser = this.getTestUserService().getTestUser();

        setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
            .body(objectMapper.writeValueAsString(new ItemCategory().setName("").setDescription("")))
            .contentType(ContentType.JSON)
            .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
            .post()
            .then().statusCode(400)
            .extract().response().asString();
    }
}
