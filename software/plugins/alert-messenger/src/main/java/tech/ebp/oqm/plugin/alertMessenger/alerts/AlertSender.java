package tech.ebp.oqm.plugin.alertMessenger.alerts;

import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;

public interface AlertSender<C extends ConnectionDetails> {
    MessageChannels messageChannel();
    void send(C connection, EventNotificationWrapper eventNotificationWrapper);
}
