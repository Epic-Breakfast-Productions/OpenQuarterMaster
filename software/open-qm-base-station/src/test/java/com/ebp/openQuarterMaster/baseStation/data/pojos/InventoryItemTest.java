package com.ebp.openQuarterMaster.baseStation.data.pojos;

import com.ebp.openQuarterMaster.baseStation.data.pojos.exceptions.UnitNotSupportedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.units.indriya.unit.Units;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class InventoryItemTest {

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

        log.info("test item json: {}", itemJson);

        InventoryItem itemBack = Utils.OBJECT_MAPPER.readValue(itemJson, InventoryItem.class);

        assertEquals(itemOne, itemBack, "Deserialized object was not equal to original.");
    }

    @Test
    public void playground() throws JsonProcessingException {
        InventoryItem.builder();
    }
}