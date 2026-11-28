package tech.ebp.oqm.plugin.alertMessenger.throttle;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@MongoEntity(collection = "eventMetrics")
public class EventMetrics extends PanacheMongoEntity {
    private ObjectId preferenceId;
    private Instant sendTime;

    /*
     * Map of databaseId to a map of EventType to count of events for that type.
     * This allows us to track how many events of each type have been sent for each database.
     * Map<PreferenceId, Map<EventTypeName, Count>>
     */
    private Map<String, Map<String, Integer>> eventCountsByDatabaseId;
}
