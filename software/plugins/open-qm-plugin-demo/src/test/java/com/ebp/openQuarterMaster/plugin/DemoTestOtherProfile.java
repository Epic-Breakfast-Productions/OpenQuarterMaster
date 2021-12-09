package com.ebp.openQuarterMaster.plugin;

import com.ebp.openQuarterMaster.plugin.testResources.ProfileTwoTestProfile;
import com.ebp.openQuarterMaster.plugin.testResources.TestResourceLifecycleManager;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
@TestProfile(ProfileTwoTestProfile.class)
@QuarkusTestResource(value = TestResourceLifecycleManager.class, initArgs = @ResourceArg(name=TestResourceLifecycleManager.OTHER_PROFILE, value="true"))
public class DemoTestOtherProfile {

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

        assertNotEquals(value, valueTwo);
    }

}