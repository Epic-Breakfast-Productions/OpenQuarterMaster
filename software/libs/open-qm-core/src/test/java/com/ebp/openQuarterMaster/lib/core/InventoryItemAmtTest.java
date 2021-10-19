package com.ebp.openQuarterMaster.lib.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class InventoryItemAmtTest {
    public InventoryItemAmt getTestItem() {
        InventoryItemAmt output = new InventoryItemAmt();
        output.setName(Faker.instance().name().name());
//        output.getName()

        return output;
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        InventoryItemAmt itemOne = getTestItem();

        String itemJson = Utils.OBJECT_MAPPER.writeValueAsString(itemOne);

        log.info("test item json: {}", itemJson);

        InventoryItemAmt itemBack = Utils.OBJECT_MAPPER.readValue(itemJson, InventoryItemAmt.class);

        assertEquals(itemOne, itemBack, "Deserialized object was not equal to original.");

    }

    @Test
    public void playground() throws JsonProcessingException {
//        InventoryItemAmt itemBack = Utils.OBJECT_MAPPER.readValue(itemJson, InventoryItemAmt.class);
        
    }
}