package tech.ebp.oqm.core.api.interfaces.endpoints.inventory;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import javax.measure.Unit;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestHTTPEndpoint(UnitsEndpoints.class)
class UnitsEndpointsTestIT extends UnitsEndpointsTest {

}