package tech.ebp.oqm.core.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.model.Utils;
import tech.ebp.oqm.core.api.model.rest.ErrorMessage;
import tech.ebp.oqm.core.api.model.rest.auth.user.UserLoginRequest;

import jakarta.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

@Slf4j
@QuarkusIntegrationTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
public class AuthIntTest {

    @Test
    public void testBadLoginNoUser() throws JsonProcessingException {
        log.info("INTEGRATION TEST");
        UserLoginRequest ulr = new UserLoginRequest("bad", "login", true);
        ErrorMessage errorMessage = given()
                .basePath(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/auth/user")
                .contentType(ContentType.JSON)
                .body(Utils.OBJECT_MAPPER.writeValueAsString(ulr))
                .when()
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract().body().as(ErrorMessage.class);

        log.info("Error Message: {}", errorMessage);
        assertEquals("User not found.", errorMessage.getDisplayMessage());
    }
}
