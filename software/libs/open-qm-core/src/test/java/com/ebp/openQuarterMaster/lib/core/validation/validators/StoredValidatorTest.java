package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.storage.stored.TrackedItem;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectValidatorTest;
import com.ebp.openQuarterMaster.lib.core.testUtils.TestConstraintValidatorContext;
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

import static com.ebp.openQuarterMaster.lib.core.validation.validators.StoredValidator.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoredValidatorTest extends ObjectValidatorTest<StoredValidator> {

    private static Stream<Arguments> validArgs() {
        return Stream.of(
                Arguments.of((Stored) null),
                Arguments.of(new Stored(StoredType.AMOUNT, null, null, Quantities.getQuantity(50, AbstractUnit.ONE), null)),
                Arguments.of(new Stored(StoredType.TRACKED, null, null, null, new HashMap<>())),
                Arguments.of(new Stored(StoredType.TRACKED, null, null, null,
                                new HashMap<>() {{
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
                Arguments.of(new Stored(StoredType.AMOUNT, null, null, Quantities.getQuantity(50, AbstractUnit.ONE), validMap), new String[]{ITEMS_LIST_NOT_NULL}),
                Arguments.of(new Stored(StoredType.TRACKED, null, null, null, null), new String[]{ITEM_LIST_WAS_NULL}),
                Arguments.of(new Stored(StoredType.AMOUNT, null, null, null, null), new String[]{AMOUNT_WAS_NULL}),
                Arguments.of(Utils.OBJECT_MAPPER.readValue("{}", Stored.class), new String[]{TYPE_WAS_NULL})//because constructor checks for null stored type
        );
    }

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
    public void invalidTest(Stored testStored, String... expectedMessages) {
        TestConstraintValidatorContext ctx = new TestConstraintValidatorContext();
        boolean result = this.validator.isValid(testStored, ctx);
        assertFalse(result);
        assertHasErrorMessages(ctx, expectedMessages);

    }
}