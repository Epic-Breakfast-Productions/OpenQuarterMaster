package tech.ebp.oqm.plugin.alertMessenger.preferences;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventType;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.ObjectType;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@MongoEntity(collection = "userNotificationConfigurations")
public class UserPreferences extends PanacheMongoEntity {

    public String userId;

    public String name;

    public String description;

    public boolean enabled;

    public String databaseId;

    @NotNull
    public Set<ObjectType> objectTypes;

    @NotNull
    public Set<EventType> eventTypes;

    @NotNull
    public List<ObjectId> connectionDetailIds;
}
