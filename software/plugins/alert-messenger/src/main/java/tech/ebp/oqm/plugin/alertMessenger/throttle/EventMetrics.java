package tech.ebp.oqm.plugin.alertMessenger.throttle;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Getter;
import lombok.Setter;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventType;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
public class EventMetrics extends PanacheMongoEntity {
    private String preferenceId;

    private Instant sentTime;

    /*
     * Map of databaseId to a map of EventType to count of events for that type.
     * This allows us to track how many events of each type have been sent for each database.
     */
    private Map<String, Map<EventType, Integer>> eventCountsByDatabaseId;
}