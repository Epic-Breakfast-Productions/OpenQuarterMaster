package tech.ebp.oqm.plugin.alertMessenger.alerts;

import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannel;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;

import java.util.List;

public interface AlertSender<C extends ConnectionDetails> {
    MessageChannel messageChannel();

    void send(C connection, List<EventNotificationWrapper> events);

    default void send(C connection, EventNotificationWrapper event) {
        this.send(connection, List.of(event));
    }
}
