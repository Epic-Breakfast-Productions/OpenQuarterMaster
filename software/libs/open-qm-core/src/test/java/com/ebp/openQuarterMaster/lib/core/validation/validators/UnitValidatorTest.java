package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.Utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class UnitValidatorTest {

    private static Stream<Arguments> validUnits(){
        return Utils.ALLOWED_UNITS.stream().map(Arguments::of);
    }
    private static Stream<Arguments> invalidUnits(){
        return Stream.of(
                Arguments.of(Units.AMPERE),
                Arguments.of(Units.DAY),
                Arguments.of(Units.CANDELA)
        );
    }

    private UnitValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new UnitValidator();
    }

    @Test
    public void nullUnitTest() {
        assertTrue(this.validator.isValid(null, null));
    }

    @ParameterizedTest(name = "validTest[{index}]")
    @MethodSource("validUnits")
    public void validTest(Unit testUnit) {
        log.info("Testing that \"{}\" is considered a valid unit.", testUnit);
        assertTrue(this.validator.isValid(testUnit, null));
    }

    @ParameterizedTest(name = "invalidTest[{index}]")
    @MethodSource("invalidUnits")
    public void invalidTest(Unit testUnit) {
        log.info("Testing that {} is considered an invalid unit.", testUnit);
        assertFalse(this.validator.isValid(testUnit, null));
    }

}