package tech.ebp.oqm.lib.core.validation.validators;

import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.testUtils.ObjectValidatorTest;
import tech.ebp.oqm.lib.core.testUtils.TestConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class UnitValidatorTest extends ObjectValidatorTest<UnitValidator> {
	
	private static Stream<Arguments> validUnits() {
		return UnitUtils.ALLOWED_UNITS.stream().map(Arguments::of);
	}
	
	private static Stream<Arguments> invalidUnits() {
		return Stream.of(
			Arguments.of(Units.AMPERE),
			Arguments.of(Units.DAY),
			Arguments.of(Units.CANDELA)
		);
	}
	
	@BeforeEach
	public void setUp() {
		oldValidator = new UnitValidator();
	}
	
	@Test
	public void nullUnitTest() {
		assertTrue(this.oldValidator.isValid(null, null));
	}
	
	@ParameterizedTest(name = "validTest[{index}]")
	@MethodSource("validUnits")
	public void validTest(Unit testUnit) {
		log.info("Testing that \"{}\" is considered a valid unit.", testUnit);
		assertTrue(this.oldValidator.isValid(testUnit, null));
	}
	
	@ParameterizedTest(name = "invalidTest[{index}]")
	@MethodSource("invalidUnits")
	public void invalidTest(Unit testUnit) {
		log.info("Testing that {} is considered an invalid unit.", testUnit);
		TestConstraintValidatorContext ctx = new TestConstraintValidatorContext();
		boolean result = this.oldValidator.isValid(testUnit, ctx);
		assertFalse(result);
		assertHasErrorMessages(ctx, "Invalid unit");
	}
	
}