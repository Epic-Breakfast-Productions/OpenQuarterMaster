package com.ebp.openQuarterMaster.plugin;

import com.ebp.openQuarterMaster.plugin.testResources.TestResourceLifecycleManager;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class)
public class DemoTest {

    @ConfigProperty(name = "some.value")
    String value;
    @ConfigProperty(name = "some.valueTwo")
    String valueTwo;

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/demo/1")
          .then()
             .statusCode(200);

        assertEquals(value, valueTwo);
    }

}