package tech.ebp.oqm.core.api.interfaces.endpoints.info;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.util.Currency;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@QuarkusIntegrationTest
@TestHTTPEndpoint(GeneralInfo.class)
class GeneralInfoTestIT extends GeneralInfoTest {

}