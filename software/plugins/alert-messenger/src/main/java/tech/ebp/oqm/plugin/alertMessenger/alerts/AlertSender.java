package tech.ebp.oqm.plugin.alertMessenger.alerts;

import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;
import tech.ebp.oqm.plugin.alertMessenger.throttle.EventMetrics;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannel;

public interface AlertSender<C extends ConnectionDetails> {
    MessageChannel messageChannel();
    void send(C connection, EventNotificationWrapper event);
    void sendMetrics(C connection, EventMetrics eventMetrics);
}
