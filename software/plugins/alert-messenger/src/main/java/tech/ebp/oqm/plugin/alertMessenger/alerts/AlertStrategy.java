package tech.ebp.oqm.plugin.alertMessenger.alerts;

import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;

public interface AlertStrategy {
    void sendAlert(EventNotificationWrapper message);
}
