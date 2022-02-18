package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class InventoryItemTest extends BasicTest {

    private static Stream<Arguments> jsonTestArgs() {
        //        Map<ObjectId, List<Stored>> validAmountMap = new HashMap<>() {{
        //            put(ObjectId.get(), List.of(new Stored(Quantities.getQuantity(5, AbstractUnit.ONE))));
        //        }};
        //        Map<ObjectId, List<Stored>> validTrackedMap = new HashMap<>() {{
        //            put(ObjectId.get(), List.of(new Stored(Map.of())));
        //        }};
    
        return Stream.of(
            Arguments.of(new AmountItem().setName(FAKER.food().ingredient())),
            Arguments.of(new TrackedItem().setTrackedItemIdentifierName("fileName").setName(FAKER.file().fileName()))
            //                Arguments.of(new InventoryItem(FAKER.name().name(), AbstractUnit.ONE).setId(ObjectId.get())),
            //                Arguments.of(new InventoryItem(FAKER.name().name(), "serial")),
            //                Arguments.of(new InventoryItem()
            //                        .setStoredType(StoredType.AMOUNT)
            //                        .setName(FAKER.name().name())
            //                        .setStorageMap(validAmountMap)
            //                ),
            //                Arguments.of(new InventoryItem()
            //                        .setStoredType(StoredType.TRACKED)
            //                        .setName(FAKER.name().name())
            //                        .setStorageMap(validTrackedMap)
            //                )
        );
    }


    @ParameterizedTest(name = "jsonTest[{index}]")
    @MethodSource("jsonTestArgs")
    public void jsonTest(InventoryItem testStored) throws JsonProcessingException {
        log.info("Item object: {}", testStored);

        String storedJson = Utils.OBJECT_MAPPER.writeValueAsString(testStored);

        log.info("Item json: {}", storedJson);

        InventoryItem deserialized = Utils.OBJECT_MAPPER.readValue(storedJson, InventoryItem.class);

        assertEquals(testStored, deserialized, "Deserialized object was not equal to original.");
    }

}