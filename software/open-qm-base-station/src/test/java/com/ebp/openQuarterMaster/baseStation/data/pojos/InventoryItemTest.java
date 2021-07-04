package com.ebp.openQuarterMaster.baseStation.data.pojos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.units.indriya.unit.Units;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class InventoryItemTest {

    public static InventoryItem getTestItem(){
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

        assertEquals(itemOne, itemBack);
    }
}