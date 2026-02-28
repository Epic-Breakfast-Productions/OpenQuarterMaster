package tech.ebp.oqm.plugin.imageSearch;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.imageSearch.testResources.testClasses.RunningServerTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GreetingResourceTest extends RunningServerTest {
    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             ;
    }

}