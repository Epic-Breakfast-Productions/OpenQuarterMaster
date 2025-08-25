package tech.ebp.oqm.core.api.model.units;

import org.apache.commons.lang3.time.StopWatch;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;
import tech.ebp.oqm.core.api.model.validation.validators.UnitValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TODO:: test invalid unit de/serialization
 */
@Slf4j
class UnitUtilsTest extends BasicTest {
	
	private static Stream<Arguments> unitsAsArgs() {
		return UnitUtils.UNIT_LIST.stream().map(Arguments::of);
	}
	
	private static Stream<Arguments> invalidUnits() {
		return Stream.of(
			Units.BECQUEREL,
			Units.DAY
		).map(Arguments::of);
	}
	
	private final UnitValidator unitValidator = new UnitValidator();
	
	@ParameterizedTest
	@MethodSource("unitsAsArgs")
	public void testStringMethods(Unit<?> unit) {
		log.info(
			"Testing unit: {}, name=\"{}\", symbol=\"{}\", dimension=\"{}\"",
			unit,
			unit.getName(),
			unit.getSymbol(),
			unit.getDimension()
		);
		
		String unitStr = UnitUtils.stringFromUnit(unit);
		
		log.info("Unit String:\"{}\"", unitStr);
		
		Unit<?> unitBack = UnitUtils.unitFromString(unitStr);
		
		assertEquals(unit, unitBack);
	}
	
	@ParameterizedTest
	@MethodSource("unitsAsArgs")
	public void testNoDuplicates(Unit<?> unit) {
		log.info(
			"Testing unit: {}, name=\"{}\", symbol=\"{}\", dimension=\"{}\"",
			unit,
			unit.getName(),
			unit.getSymbol(),
			unit.getDimension()
		);
		
		boolean found = false;
		for (Unit<?> curUnit : UnitUtils.UNIT_LIST) {
			if (unit.equals(curUnit)) {
				if (found) {
					fail("Unit is in the list more than once.");
				} else {
					found = true;
				}
			}
		}
	}
	
	@ParameterizedTest
	@MethodSource("unitsAsArgs")
	public void testUnitSerialization(Unit<?> unit) throws JsonProcessingException {
		log.info(
			"Testing unit: {}, name=\"{}\", symbol=\"{}\", dimension=\"{}\"",
			unit,
			unit.getName(),
			unit.getSymbol(),
			unit.getDimension()
		);
		
		String serialized = ObjectUtils.OBJECT_MAPPER.writeValueAsString(unit);
		log.info("Serialized unit: \"{}\"", serialized);
		Unit<?> deserialized = ObjectUtils.OBJECT_MAPPER.readValue(serialized, Unit.class);
		
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
		//TODO:: do something here
	}
	
	@ParameterizedTest
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
	
	@Test
	public void testTimeForReinit() {
		StopWatch sw = StopWatch.createStarted();
		UnitUtils.reInitUnitCollections();
		sw.stop();
		log.info("Took {} to reinit units.", sw);
	}
	
	@Test
	public void testUnitOrder() {
		UnitCategory firstCat = (UnitCategory) ((LinkedHashMap) UnitUtils.UNIT_CATEGORY_MAP).keySet().stream().findFirst().get();
		
		assertEquals(UnitCategory.Number, firstCat);
		
		assertEquals(OqmProvidedUnits.UNIT, UnitUtils.UNIT_CATEGORY_MAP.get(firstCat).stream().findFirst().get());
	}
	
	@Test
	public void testUnitCompatabilityOrder() {
		for (Map.Entry<Unit<?>, Set<Unit<?>>> curEntry : UnitUtils.UNIT_COMPATIBILITY_MAP.entrySet()) {
			
			assertEquals(
				curEntry.getKey(),
				curEntry.getValue().stream().findFirst().get()
			);
		}
	}
	
	private static Stream<Arguments> quantityCompareArgs(){
		return Stream.of(
			Arguments.of(null, null, false),
			Arguments.of(Quantities.getQuantity(2, OqmProvidedUnits.UNIT), Quantities.getQuantity(3, OqmProvidedUnits.UNIT), false),
			Arguments.of(Quantities.getQuantity(2, OqmProvidedUnits.UNIT), Quantities.getQuantity(2, OqmProvidedUnits.UNIT), true),
			Arguments.of(Quantities.getQuantity(1, LibUnits.UnitProxies.KILOGRAM), Quantities.getQuantity(1001, LibUnits.UnitProxies.GRAM), false),
			Arguments.of(Quantities.getQuantity(1, LibUnits.UnitProxies.KILOGRAM), Quantities.getQuantity(1000, LibUnits.UnitProxies.GRAM), true)
		);
	}
	
	@ParameterizedTest
	@MethodSource("quantityCompareArgs")
	public <T extends Quantity<T>> void testAtOrUnderThreshold(Quantity<T> threshold, Quantity<T> amount, boolean expected) {
		assertEquals(expected, UnitUtils.atOrUnderThreshold(threshold, amount));
	}
	
	//	@ParameterizedTest
	//	@MethodSource("unitsAsArgs")
	//	public void testFormat(Unit<?> unit) {
	//		log.info(
	//			"Format for {}: \"{}\"",
	//			unit,
	//			SimpleUnitFormat.getInstance().format(unit)
	//		);
	//	}
	
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