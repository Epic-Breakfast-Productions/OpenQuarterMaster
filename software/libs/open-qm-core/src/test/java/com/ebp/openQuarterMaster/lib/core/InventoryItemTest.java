package com.ebp.openQuarterMaster.lib.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryItemTest {
    public InventoryItem getTestItem() {
        return InventoryItem
                .builder()
                .name("test item")
                .build();
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        InventoryItem itemOne = getTestItem();

        String itemJson = Utils.OBJECT_MAPPER.writeValueAsString(itemOne);

//        log.info("test item json: {}", itemJson);

        InventoryItem itemBack = Utils.OBJECT_MAPPER.readValue(itemJson, InventoryItem.class);

        assertEquals(itemOne, itemBack, "Deserialized object was not equal to original.");
    }

    @Test
    public void playground() throws JsonProcessingException {
        InventoryItem.builder();
    }
}