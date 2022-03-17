package com.ebp.openQuarterMaster.baseStation.endpoints.info;

import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import javax.measure.Unit;
import javax.ws.rs.core.MediaType;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(GeneralInfo.class)
class GeneralInfoTest extends RunningServerTest {
	
	public static Stream<Arguments> getUnitsArgs() {
		return UnitUtils.ALLOWED_UNITS.stream().map(Arguments::of);
	}
	
	@Inject
	ObjectMapper mapper;
	
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
}