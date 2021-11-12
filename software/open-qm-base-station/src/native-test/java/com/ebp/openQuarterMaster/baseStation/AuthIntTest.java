package com.ebp.openQuarterMaster.baseStation;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.rest.ErrorMessage;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusIntegrationTest
public class AuthIntTest {

    @Test
    public void testBadLoginNoUser() throws JsonProcessingException {
        UserLoginRequest ulr = new UserLoginRequest("bad", "login", true);
        ErrorMessage errorMessage = given()
                .basePath("/api/user/auth")
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
