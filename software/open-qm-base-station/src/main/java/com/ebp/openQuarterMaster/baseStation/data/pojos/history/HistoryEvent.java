package com.ebp.openQuarterMaster.baseStation.data.pojos.history;

import lombok.*;

import javax.validation.constraints.NotNull;
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
    @NonNull
    private EventType type;
    /** The user that performed the event */
    @NonNull
    @NotNull
    private UUID userId;
    /** When the event occurred */
    @Builder.Default
    @NonNull
    @NotNull
    private ZonedDateTime timestamp = ZonedDateTime.now();
    /** Description of the event */
    @Builder.Default
    @NonNull
    @NotNull
    private String description = "";
}
