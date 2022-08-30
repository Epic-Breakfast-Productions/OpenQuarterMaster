package tech.ebp.oqm.baseStation.interfaces.endpoints.info;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.lib.core.UnitUtils;

import javax.measure.Unit;
import javax.ws.rs.core.MediaType;
import java.util.Currency;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@QuarkusTest
@TestHTTPEndpoint(GeneralInfo.class)
class GeneralInfoTest extends RunningServerTest {
	
	@ConfigProperty(name = "service.ops.currency")
	Currency currency;
	
	public static Stream<Arguments> getUnitsArgs() {
		return UnitUtils.ALLOWED_UNITS.stream().map(Arguments::of);
	}
	
	@ParameterizedTest
	@MethodSource("getUnitsArgs")
	public void testGetCompatibleUnitsJson(Unit<?> unit) {
		given()
			.accept(MediaType.APPLICATION_JSON)
			.get("unitCompatibility/" + UnitUtils.stringFromUnit(unit))
			.then()
			.statusCode(200);
	}
	
	@ParameterizedTest
	@MethodSource("getUnitsArgs")
	public void testGetCompatibleUnitsHtml(Unit<?> unit) {
		given()
			.accept(MediaType.TEXT_HTML)
			.get("unitCompatibility/" + UnitUtils.stringFromUnit(unit))
			.then()
			.statusCode(200);
	}
	
	@Test
	public void testGetCurrency(){
		ValidatableResponse response = given()
			.get("currency")
			.then()
			.statusCode(200);
		
		assertEquals(
			this.currency,
			response.extract().as(Currency.class)
		);
	}
}