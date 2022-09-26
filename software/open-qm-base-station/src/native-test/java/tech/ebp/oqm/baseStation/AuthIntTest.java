package tech.ebp.oqm.baseStation;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.lib.core.Utils;
import tech.ebp.oqm.lib.core.rest.ErrorMessage;
import tech.ebp.oqm.lib.core.rest.user.UserLoginRequest;

import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;

@Slf4j
@QuarkusIntegrationTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
public class AuthIntTest {

    @Test
    public void testBadLoginNoUser() throws JsonProcessingException {
        log.info("INTEGRATION TEST");
        UserLoginRequest ulr = new UserLoginRequest("bad", "login", true);
        ErrorMessage errorMessage = given()
                .basePath("/api/auth/user")
                .contentType(ContentType.JSON)
                .body(Utils.OBJECT_MAPPER.writeValueAsString(ulr))
                .when()
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract().body().as(ErrorMessage.class);

        log.info("Error Message: {}", errorMessage);
        assertEquals("User not found.", errorMessage.getError());
    }
}
