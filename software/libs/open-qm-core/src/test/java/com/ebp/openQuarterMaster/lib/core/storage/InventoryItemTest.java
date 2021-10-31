package com.ebp.openQuarterMaster.lib.core.storage;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.storage.stored.TrackedItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class InventoryItemTest {

    //TODO:: test with objectIds
    private static Stream<Arguments> jsonTestArgs(){
        Map<ObjectId, Stored> validAmountMap = new HashMap<>(){{
            put(ObjectId.get(), new Stored(Quantities.getQuantity(5, AbstractUnit.ONE)));
        }};
        Map<ObjectId, Stored> validTrackedMap = new HashMap<>(){{
//            put(ObjectId.get(), new Stored());
        }};

        return Stream.of(
                Arguments.of(new InventoryItem(Faker.instance().name().name(), AbstractUnit.ONE)),
                Arguments.of(new InventoryItem(Faker.instance().name().name(), "serial")),
                Arguments.of(new InventoryItem().setStoredType(StoredType.AMOUNT).setName(Faker.instance().name().name())),
                Arguments.of(new InventoryItem()
                        .setStoredType(StoredType.TRACKED)
                        .setName(Faker.instance().name().name())
                        .setStorageMap(validTrackedMap)
                )
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