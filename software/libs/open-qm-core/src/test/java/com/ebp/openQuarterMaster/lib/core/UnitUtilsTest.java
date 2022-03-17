package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.validation.validators.UnitValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TODO:: test invalid unit de/serialization
 */
@Slf4j
class UnitUtilsTest {
	
	private static Stream<Arguments> unitsAsArgs() {
		return UnitUtils.ALLOWED_UNITS.stream().map(Arguments::of);
	}
	
	private static Stream<Arguments> invalidUnits() {
		return Stream.of(
			Units.BECQUEREL,
			Units.DAY
		).map(Arguments::of);
	}
	
	private final UnitValidator unitValidator = new UnitValidator();
	
	@ParameterizedTest(name = "testUnitSerialization[{index}]({0})")
	@MethodSource("unitsAsArgs")
	public void testUnitSerialization(Unit<?> unit) throws JsonProcessingException {
		log.info(
			"Testing unit: {}, name=\"{}\", symbol=\"{}\", dimension=\"{}\"",
			unit,
			unit.getName(),
			unit.getSymbol(),
			unit.getDimension()
		);
		
		String serialized = Utils.OBJECT_MAPPER.writeValueAsString(unit);
		log.info("Serialized unit: \"{}\"", serialized);
		Unit<?> deserialized = Utils.OBJECT_MAPPER.readValue(serialized, Unit.class);
		
		log.info(
			"Deserialized unit: {}, name=\"{}\", symbol=\"{}\", dimension=\"{}\"",
			deserialized,
			deserialized.getName(),
			deserialized.getSymbol(),
			deserialized.getDimension()
		);
		
		assertEquals(unit, deserialized);
		assertTrue(this.unitValidator.isValid(deserialized, null));
	}
	
	@Test
	public void testUnitCompatibilityMap() {
		log.info("Map: {}", UnitUtils.UNIT_COMPATIBILITY_MAP);
	}
	
	@ParameterizedTest(name = "testUnitHasNameSymbol[{index}]({0})")
	@MethodSource("unitsAsArgs")
	public void testUnitHasNameSymbol(Unit<?> unit) {
		log.info(
			"Testing unit: {}, name=\"{}\", symbol=\"{}\", dimension=\"{}\"",
			unit,
			unit.getName(),
			unit.getSymbol(),
			unit.getDimension()
		);
		assertNotNull(unit.getName(), "Unit had no name");
		assertNotNull(unit.getSymbol(), "Unit had no symbol");
	}
	
	//TODO:: figure out how to serialize invalid units
	//    @ParameterizedTest(name = "invalidUnitSerialization[{index}]({0})")
	//    @MethodSource("invalidUnits")
	//    public void invalidUnitSerialization(Unit unit) throws JsonProcessingException {
	//        log.info("Testing unit: {}, name=\"{}\", symbol=\"{}\", dimension=\"{}\"", unit, unit.getName(), unit.getSymbol(), unit.getDimension());
	//
	//        String serialized = Utils.OBJECT_MAPPER.writeValueAsString(unit);
	//        log.info("Serialized unit: {}", serialized);
	//        Unit deserialized = Utils.OBJECT_MAPPER.readValue(serialized, Unit.class);
	//
	//        log.info("Deserialized unit: {}, name=\"{}\", symbol=\"{}\", dimension=\"{}\"", deserialized, deserialized.getName(), deserialized.getSymbol(), deserialized.getDimension());
	//    }
	
}