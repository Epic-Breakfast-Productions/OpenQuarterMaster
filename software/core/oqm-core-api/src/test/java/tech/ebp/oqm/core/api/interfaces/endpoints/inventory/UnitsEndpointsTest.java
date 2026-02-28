package tech.ebp.oqm.core.api.interfaces.endpoints.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.units.ConvertRequest;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.core.api.interfaces.endpoints.inventory.UnitsEndpoints;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Tag("integration")
@QuarkusTest
@TestHTTPEndpoint(UnitsEndpoints.class)
class UnitsEndpointsTest extends RunningServerTest {
	
	public static Stream<Arguments> getUnitsArgs() {
		return UnitUtils.UNIT_LIST.stream().map(Arguments::of);
	}
	public static Stream<Arguments> getUnitsCompatMapArgs() {
		ArrayList<Arguments> args = new ArrayList<>();
		
		for(Map.Entry<Unit<?>, Set<Unit<?>>> curUnitEntry : UnitUtils.UNIT_COMPATIBILITY_MAP.entrySet()){
			for(Unit<?> curCompatUnit : curUnitEntry.getValue()){
				args.add(Arguments.of(curUnitEntry.getKey(), curCompatUnit));
			}
		}
		
		return args.stream();
	}
	public static Stream<Arguments> getUnitsCompatMapListArgs() {
		ArrayList<Arguments> args = new ArrayList<>();
		
		for(Map.Entry<Unit<?>, Set<Unit<?>>> curUnitEntry : UnitUtils.UNIT_COMPATIBILITY_MAP.entrySet()){
			args.add(Arguments.of(curUnitEntry.getKey(), curUnitEntry.getValue()));
		}
		
		return args.stream();
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
	
	@Test
	public void testGetDeriveTypes() {
		String deriveTypes = given()
								 .accept(MediaType.APPLICATION_JSON)
								 .get("deriveTypes")
								 .then()
								 .statusCode(200)
								 .extract()
								 .asString();
		log.info("Derive Types: {}", deriveTypes);
	}
	
	@Test
	public void testGetDimensions() {
		String deriveTypes = given()
								 .accept(MediaType.APPLICATION_JSON)
								 .get("dimensions")
								 .then()
								 .statusCode(200)
								 .extract()
								 .asString();
		log.info("Dimensions: {}", deriveTypes);
	}
	
	@ParameterizedTest
	@MethodSource("getUnitsCompatMapArgs")
	public void testQuantityConvert(Unit<?> quantityUnit, Unit<?> toUnit) throws JsonProcessingException {
		Quantity<?> quantity = Quantities.getQuantity(5, quantityUnit);
		ConvertRequest request = ConvertRequest.builder().quantity(quantity).newUnit(toUnit).build();
		
		log.info("Calling convertEndpoint with: {}", request);
		ValidatableResponse response = given()
										   .accept(MediaType.APPLICATION_JSON)
										   .contentType(MediaType.APPLICATION_JSON)
										   .body(ObjectUtils.OBJECT_MAPPER.writeValueAsString(request))
										   .put("convert")
										   .then();
		
		log.info("Response status: {} Body: {}", response.extract().statusCode(), response.extract().body().asString());
		
		response.statusCode(200);
		log.info("Resulting quantity: {}", ObjectUtils.OBJECT_MAPPER.readValue(response.extract().body().asString(), Quantity.class));
	}
	
	@ParameterizedTest
	@MethodSource("getUnitsCompatMapListArgs")
	public void testQuantityListConvert(Unit<?> quantityUnit, Set<Unit<?>> toUnit) throws JsonProcessingException {
		List<ConvertRequest> request = new ArrayList<>();
		
		for(Unit<?> curUnit : toUnit){
			Quantity<?> quantity = Quantities.getQuantity(5, quantityUnit);
			request.add(
				ConvertRequest.builder().quantity(quantity).newUnit(curUnit).build()
			);
		}
		
		log.info("Calling convertEndpoint with: {}", request);
		ValidatableResponse response = given()
										   .accept(MediaType.APPLICATION_JSON)
										   .contentType(MediaType.APPLICATION_JSON)
										   .body(ObjectUtils.OBJECT_MAPPER.writeValueAsString(request))
										   .put("convert")
										   .then();
		
		log.info("Response status: {} Body: {}", response.extract().statusCode(), response.extract().body().asString());
		
		response.statusCode(200);
		log.info("Resulting quantity: {}", ObjectUtils.OBJECT_MAPPER.readValue(response.extract().body().asString(), new TypeReference<List<Quantity<?>>>() { }));
	}
	
	//TODO:: negative test for incompatible units
}