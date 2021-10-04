package com.ebp.openQuarterMaster.baseStation.data.pojos.history;

import com.ebp.openQuarterMaster.baseStation.data.pojos.InventoryItem;
import com.ebp.openQuarterMaster.baseStation.data.pojos.PojoTest;
import com.ebp.openQuarterMaster.baseStation.data.pojos.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class HistoryEventTest  extends PojoTest<HistoryEvent> {

    @Override
    public HistoryEvent getBasicTestItem() {
        return HistoryEvent.builder()
                .type(EventType.CREATE)
                .userId(UUID.randomUUID())
                .description("someDesc")
                .build();
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        HistoryEvent eventOne = getBasicTestItem();

        String eventJson = Utils.OBJECT_MAPPER.writeValueAsString(eventOne);

        log.info("test event json: {}", eventJson);

        HistoryEvent eventBack = Utils.OBJECT_MAPPER.readValue(eventJson, HistoryEvent.class);

        assertEquals(eventOne, eventBack, "Deserialized object was not equal to original.");
    }


}