package tech.ebp.oqm.plugin.alertMessenger.throttle;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class AlertUserBucket extends PanacheMongoEntity {
    private String userId;
    private List<ObjectId> alertEventId;
    private Instant sendAfter;
}
