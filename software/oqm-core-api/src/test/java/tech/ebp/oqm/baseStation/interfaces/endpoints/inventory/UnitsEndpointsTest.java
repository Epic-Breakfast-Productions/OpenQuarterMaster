package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;

import javax.measure.Unit;
import java.util.Currency;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@QuarkusTest
@TestHTTPEndpoint(UnitsEndpoints.class)
class UnitsEndpointsTest extends RunningServerTest {
	
	@ConfigProperty(name = "service.ops.currency")
	Currency currency;
	
	public static Stream<Arguments> getUnitsArgs() {
		return UnitUtils.UNIT_LIST.stream().map(Arguments::of);
	}
	
	@ParameterizedTest
	@MethodSource("getUnitsArgs")
	public void testGetCompatibleUnitsJson(Unit<?> unit) {
		given()
			.accept(MediaType.APPLICATION_JSON)
			.get("compatibility/" + UnitUtils.stringFromUnit(unit))
			.then()
			.statusCode(200);
	}
}