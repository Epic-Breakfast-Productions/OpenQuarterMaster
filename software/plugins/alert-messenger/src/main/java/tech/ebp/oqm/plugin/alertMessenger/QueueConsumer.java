package tech.ebp.oqm.plugin.alertMessenger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.alerts.AlertHandler;

@Slf4j
@ApplicationScoped
public class QueueConsumer {

    @Inject
    AlertHandler alertHandler;

    @Incoming("oqm-core-all-events")
    public void receive(EventNotificationWrapper message) {
        log.debug("Received message: {}", message);
        alertHandler.handleAlert(message);
    }
}
