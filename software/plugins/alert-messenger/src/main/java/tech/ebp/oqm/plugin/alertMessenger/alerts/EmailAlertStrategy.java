package tech.ebp.oqm.plugin.alertMessenger.alerts;

import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;

@ApplicationScoped
public class EmailAlertStrategy implements AlertStrategy {

    @Override
    public void sendAlert(EventNotificationWrapper message) {
        //TODO
    }
}
