package tech.ebp.oqm.plugin.alertMessenger;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;

@Slf4j
@ApplicationScoped
public class QueueConsumer {

    @Incoming("oqm-core-all-events")
    public void receive(EventNotificationWrapper message) {
        this.processMessage(message);
    }

    private void processMessage(EventNotificationWrapper message) {
        // get all users subscribed to this event type and object type (iterator)
        // and send them a notification with strategy pattern.
    }
}

