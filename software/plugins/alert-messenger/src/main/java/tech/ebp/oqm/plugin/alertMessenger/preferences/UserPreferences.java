package tech.ebp.oqm.plugin.alertMessenger.preferences;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventType;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.ObjectType;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@MongoEntity(collection = "userNotificationConfigurations")
public class UserPreferences extends PanacheMongoEntity {

    @NotEmpty
    public String userId;

    @NotNull
    public Set<ObjectType> objectTypes;

    @NotNull
    public Set<EventType> eventTypes;

    @NotNull
    public Map<MessageChannels, String> messageChannels;
}
