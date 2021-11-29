package com.ebp.openQuarterMaster.plugin;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class demoTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/demo/1")
          .then()
             .statusCode(200);
    }

}