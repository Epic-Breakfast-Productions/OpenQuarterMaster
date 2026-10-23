package tech.ebp.oqm.plugin.alertMessenger.throttle;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;

import java.time.Instant;

@MongoEntity(collection = "alertEvents")
@Getter
@Setter
public class AlertEvent extends PanacheMongoEntity {
    private EventNotificationWrapper eventNotificationWrapper;
    private Instant createdAt;

    public AlertEvent(EventNotificationWrapper notification) {
        this.eventNotificationWrapper = notification;
        this.createdAt = Instant.now();
    }
}
