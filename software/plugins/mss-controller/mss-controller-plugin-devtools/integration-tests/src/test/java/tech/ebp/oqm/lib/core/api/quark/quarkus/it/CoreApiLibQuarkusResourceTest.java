package tech.ebp.oqm.lib.core.api.quark.quarkus.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CoreApiLibQuarkusResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/core-api-lib-quarkus")
                .then()
                .statusCode(200)
                .body(is("Hello core-api-lib-quarkus"));
    }
}
