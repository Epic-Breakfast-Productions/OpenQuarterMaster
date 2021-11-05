package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.storage.stored.TrackedItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StoredValidatorTest {

    private static Stream<Arguments> validArgs(){
        return Stream.of(
                Arguments.of((Stored)null),
                Arguments.of(new Stored(StoredType.AMOUNT, Quantities.getQuantity(50, AbstractUnit.ONE), null)),
                Arguments.of(new Stored(StoredType.TRACKED, null, new HashMap<>())),
                Arguments.of(new Stored(StoredType.TRACKED, null,
                                new HashMap<>(){{
                                    put(Faker.instance().idNumber().valid(), new TrackedItem(UUID.randomUUID()));
                                }}
                        )
                )
        );
    }

    private static Stream<Arguments> invalidArgs() throws JsonProcessingException {
        Map<String, TrackedItem> validMap = new HashMap<>(){{
            put(Faker.instance().idNumber().valid(), new TrackedItem(UUID.randomUUID()));
        }};
        return Stream.of(
                Arguments.of(new Stored(StoredType.AMOUNT, Quantities.getQuantity(50, AbstractUnit.ONE), validMap)),
                Arguments.of(new Stored(StoredType.TRACKED, null, null)),
                Arguments.of(new Stored(StoredType.AMOUNT, null, null)),
                Arguments.of(new Stored(StoredType.AMOUNT, null, new HashMap<>(){{put(null, new TrackedItem(UUID.randomUUID()));}})),
                Arguments.of(new Stored(StoredType.AMOUNT, null, new HashMap<>(){{put("", new TrackedItem(UUID.randomUUID()));}})),
                Arguments.of(new Stored(StoredType.AMOUNT, null, new HashMap<>(){{put(" ", new TrackedItem(UUID.randomUUID()));}})),
                Arguments.of(new Stored(StoredType.AMOUNT, null, new HashMap<>(){{put("\t", new TrackedItem(UUID.randomUUID()));}})),
                Arguments.of(new Stored(StoredType.AMOUNT, null, new HashMap<>(){{put("\n", new TrackedItem(UUID.randomUUID()));}})),
                Arguments.of(new Stored(StoredType.AMOUNT, null, new HashMap<>(){{put("\r", new TrackedItem(UUID.randomUUID()));}})),
                Arguments.of(new Stored(StoredType.AMOUNT, null, new HashMap<>(){{put(Faker.instance().idNumber().valid(), null);}})),
                Arguments.of(Utils.OBJECT_MAPPER.readValue("{}", Stored.class))//because constructor checks for null stored type
        );
    }


    private StoredValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new StoredValidator();
    }

    @ParameterizedTest(name = "validTest[{index}]")
    @MethodSource("validArgs")
    public void validTest(Stored testStored) {
        boolean result = this.validator.isValid(testStored, null);
        assertTrue(result);
    }

    @ParameterizedTest(name = "invalidTest[{index}]")
    @MethodSource("invalidArgs")
    public void invalidTest(Stored testStored) {
        //TODO:: test with error messages
        boolean result = this.validator.isValid(testStored, null);
        assertFalse(result);
    }
}