package tech.ebp.oqm.plugin.alertMessenger.throttle;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;

@MongoEntity(collection = "alertEvents")
@Getter
@Setter
public class AlertEvent extends PanacheMongoEntity {
    private EventNotificationWrapper eventNotificationWrapper;

    public AlertEvent(EventNotificationWrapper notification) {
        this.eventNotificationWrapper = notification;
    }
    //TODO: add mongo ttl index for 30d
}
