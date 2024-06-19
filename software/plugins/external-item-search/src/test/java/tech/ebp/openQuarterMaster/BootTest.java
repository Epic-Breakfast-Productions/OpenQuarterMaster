package tech.ebp.openQuarterMaster;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class BootTest {
    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/api/v1/providers")
          .then()
             .statusCode(200);
    }

}