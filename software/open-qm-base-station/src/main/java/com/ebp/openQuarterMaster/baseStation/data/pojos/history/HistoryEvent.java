package com.ebp.openQuarterMaster.baseStation.data.pojos.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Describes an event in an object's history.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryEvent {
    /** The type of event that occurred */
    private EventType type;
    /** The user that performed the event */
    private UUID userId;
    /** When the event occurred */
    @Builder.Default
    private ZonedDateTime timestamp = ZonedDateTime.now();
    /** Description of the event */
    @Builder.Default
    private String description = "";
}
