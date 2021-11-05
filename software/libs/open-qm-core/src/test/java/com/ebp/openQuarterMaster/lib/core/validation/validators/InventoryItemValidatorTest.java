package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.TrackedItem;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectValidatorTest;
import com.ebp.openQuarterMaster.lib.core.testUtils.TestConstraintValidatorContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.unit.Units;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.ebp.openQuarterMaster.lib.core.validation.validators.InventoryItemValidator.*;
import static org.junit.jupiter.api.Assertions.*;

class InventoryItemValidatorTest extends ObjectValidatorTest<InventoryItemValidator> {

    private static Stream<Arguments> validArgs() {
        Map<ObjectId, Stored> mapWithGram = new LinkedHashMap<>() {{
            put(ObjectId.get(), new Stored(2, Units.GRAM));
        }};
        Map<ObjectId, Stored> mapWithTracked = new LinkedHashMap<>() {{
            put(ObjectId.get(), new Stored(new HashMap<String, TrackedItem>()));
        }};
        return Stream.of(
                Arguments.of((InventoryItem) null),
                Arguments.of(new InventoryItem("hello", AbstractUnit.ONE)),
                Arguments.of(new InventoryItem("hello", "serial")),
                Arguments.of(new InventoryItem("hello", Units.GRAM).setStorageMap(mapWithGram)),
                Arguments.of(new InventoryItem("hello", "serial").setStorageMap(mapWithTracked))

        );
    }

    private static Stream<Arguments> invalidArgs() throws JsonProcessingException {
        Map<ObjectId, Stored> mapWithGram = new LinkedHashMap<>() {{
            put(ObjectId.get(), new Stored(2, Units.GRAM));
        }};
        Map<ObjectId, Stored> mapWithTracked = new LinkedHashMap<>() {{
            put(ObjectId.get(), new Stored(new HashMap<String, TrackedItem>()));
        }};

        Map<ObjectId, Stored> mixedMap = new LinkedHashMap<>() {{
            put(ObjectId.get(), new Stored(2, AbstractUnit.ONE));
            put(ObjectId.get(), new Stored(new HashMap<String, TrackedItem>()));
            put(ObjectId.get(), new Stored(2, Units.GRAM));
        }};


        return Stream.of(
                Arguments.of(Utils.OBJECT_MAPPER.readValue("{}", InventoryItem.class), new String[]{STORED_TYPE_WAS_NULL}),
                Arguments.of(new InventoryItem("hello", AbstractUnit.ONE).setTrackedItemIdentifierName("hello"), new String[]{TRACKED_IDENTIFIER_NAME_WHEN_IT_SHOULDN_T}),
                Arguments.of(new InventoryItem("hello", AbstractUnit.ONE).setStorageMap(mapWithTracked), new String[]{NOT_AMOUNT}),
                Arguments.of(new InventoryItem("hello", AbstractUnit.ONE).setStorageMap(mapWithGram), new String[]{INCOMPATIBLE_UNITS}),
                Arguments.of(new InventoryItem("hello", AbstractUnit.ONE).setStorageMap(mixedMap), new String[]{INCOMPATIBLE_UNITS, NOT_AMOUNT}),
                Arguments.of(new InventoryItem("hello", "serial").setUnit(Units.GRAM), new String[]{NOT_ONE}),
                Arguments.of(new InventoryItem("hello", "serial").setStorageMap(mixedMap), new String[]{NOT_ALL_TRACKED_TYPE})
        );
    }

    @BeforeEach
    public void setUp() {
        this.validator = new InventoryItemValidator();
    }

    @ParameterizedTest(name = "validTest[{index}]")
    @MethodSource("validArgs")
    public void validTest(InventoryItem testItem) {
        boolean result = this.validator.isValid(testItem, null);
        assertTrue(result);
    }

    @ParameterizedTest(name = "invalidTest[{index}]")
    @MethodSource("invalidArgs")
    public void invalidTest(InventoryItem testItem, String... expectedMessages) {
        TestConstraintValidatorContext ctx = new TestConstraintValidatorContext();
        boolean result = this.validator.isValid(testItem, ctx);
        assertFalse(result);
        assertHasErrorMessages(ctx, expectedMessages);
    }
}