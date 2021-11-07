package com.ebp.openQuarterMaster.lib.core.history;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryEventTest extends BasicTest {
    public HistoryEvent getBasicTestItem() {
        return HistoryEvent.builder()
                .type(EventType.CREATE)
                .userId(ObjectId.get())
                .description("someDesc")
                .build();
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        HistoryEvent eventOne = getBasicTestItem();

        String eventJson = Utils.OBJECT_MAPPER.writeValueAsString(eventOne);

//        log.info("test event json: {}", eventJson);

        HistoryEvent eventBack = Utils.OBJECT_MAPPER.readValue(eventJson, HistoryEvent.class);

        assertEquals(eventOne, eventBack, "Deserialized object was not equal to original.");
    }

}