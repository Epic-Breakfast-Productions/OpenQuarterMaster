package com.ebp.openQuarterMaster.baseStation.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.endpoints.inventory.items.InventoryItemsCrud;
import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
@TestHTTPEndpoint(InventoryItemsCrud.class)
class AuthTest extends RunningServerTest {
    @Inject
    ObjectMapper objectMapper;

    @Test
    public void testBadLogin() throws JsonProcessingException {
        //TODO:: more
        UserLoginRequest ulr = new UserLoginRequest("bad", "login", true);
        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(ulr))
                .when()
                .post()
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
//                .body("error", )
        ;
    }
}