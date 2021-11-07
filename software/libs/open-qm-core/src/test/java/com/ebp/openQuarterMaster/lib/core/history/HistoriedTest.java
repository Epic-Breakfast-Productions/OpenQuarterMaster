package com.ebp.openQuarterMaster.lib.core.history;

import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HistoriedTest extends BasicTest {
    public Historied getBasicTestItem() {
        return new Historied() {};
    }

    @Test
    public void testUpdated() {
        Historied o = this.getBasicTestItem();

        assertTrue(o.getHistory().isEmpty());
        assertEquals(0, o.getHistory().size());

        Historied o2 = o.updated(
                HistoryEvent.builder()
                        .type(EventType.CREATE)
                        .userId(ObjectId.get())
                        .build()
        );
        assertSame(o, o2);
        assertEquals(1, o.getHistory().size());

        o2 = o.updated(
                HistoryEvent.builder()
                        .type(EventType.UPDATE)
                        .userId(ObjectId.get())
                        .build()
        );

        assertTrue(o.getHistory().get(0).getTimestamp().isAfter(o.getHistory().get(1).getTimestamp()));
    }

    @Test
    public void testUpdatedCreate() {
        Historied o = this.getBasicTestItem();

        o.updated(HistoryEvent.builder()
                .type(EventType.CREATE)
                .userId(ObjectId.get())
                .build()
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            o.updated(HistoryEvent.builder()
                    .type(EventType.CREATE)
                    .userId(ObjectId.get())
                    .build());
        });
    }

    @Test
    public void testUpdatedUpdateFirst() {
        Historied o = this.getBasicTestItem();

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            o.updated(HistoryEvent.builder()
                    .type(EventType.UPDATE)
                    .userId(ObjectId.get())
                    .build());
        });
    }

    @Test
    public void testGetLastUpdate() {
        Historied o = this.getBasicTestItem();

        o.updated(
                HistoryEvent.builder()
                        .type(EventType.CREATE)
                        .userId(ObjectId.get())
                        .build()
        );
        o.updated(
                HistoryEvent.builder()
                        .type(EventType.UPDATE)
                        .userId(ObjectId.get())
                        .build()
        );

        HistoryEvent event = o.lastHistoryEvent();

        assertEquals(o.getHistory().get(0), event);
    }

    @Test
    public void testGetLastUpdateTime() {
        Historied o = this.getBasicTestItem();

        o.updated(
                HistoryEvent.builder()
                        .type(EventType.CREATE)
                        .userId(ObjectId.get())
                        .build()
        );
        o.updated(
                HistoryEvent.builder()
                        .type(EventType.UPDATE)
                        .userId(ObjectId.get())
                        .build()
        );

        ZonedDateTime eventTime = o.lastHistoryEventTime();

        assertEquals(o.getHistory().get(0).getTimestamp(), eventTime);
    }


}