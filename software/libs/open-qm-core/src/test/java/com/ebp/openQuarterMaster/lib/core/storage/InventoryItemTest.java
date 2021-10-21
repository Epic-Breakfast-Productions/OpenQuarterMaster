package com.ebp.openQuarterMaster.lib.core.storage;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.storage.stored.TrackedItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class InventoryItemTest {
    private static Stream<Arguments> jsonTestArgs(){
        return Stream.of(
                Arguments.of(new InventoryItem().setStoredType(StoredType.AMOUNT).setName("hello"))
        );
    }


    @ParameterizedTest(name = "jsonTest[{index}]")
    @MethodSource("jsonTestArgs")
    public void jsonTest(InventoryItem testStored) throws JsonProcessingException {
        String storedJson = Utils.OBJECT_MAPPER.writeValueAsString(testStored);

        log.info("Item object: {}", testStored);
        log.info("Item json: {}", storedJson);

        InventoryItem deserialized = Utils.OBJECT_MAPPER.readValue(storedJson, InventoryItem.class);

        assertEquals(testStored, deserialized, "Deserialized object was not equal to original.");
    }

}