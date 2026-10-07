package tech.ebp.oqm.plugin.alertMessenger.alerts;

import io.quarkus.arc.All;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferences;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferencesService;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class AlertHandler {

    @Inject
    @All
    List<AlertSender<? extends ConnectionDetails>> senderInstances;

    @Inject
    UserPreferencesService userPreferencesService;

    private Map<MessageChannels, AlertSender> alertSenders;

    @PostConstruct
    void initializeSenders() {
        alertSenders = senderInstances.stream()
            .collect(Collectors.toUnmodifiableMap(
                AlertSender::messageChannel,
                Function.identity()
            ));
        log.debug("Initialized alert senders: {}", alertSenders.keySet());
    }

    public void handleAlert(EventNotificationWrapper message) {
        Iterator<UserPreferences> iterator = userPreferencesService.getIterator();
        while (iterator.hasNext()) {
            UserPreferences preferences = iterator.next();
            if (preferences.objectTypes.contains(message.getObjectType()) || preferences.eventTypes.contains(message.getEventType())) {
                for (ConnectionDetails connectionDetails : preferences.getConnectionDetails()) {
                    log.debug("Connection details: {}", connectionDetails);
                    AlertSender sender = alertSenders.get(connectionDetails.getType());
                    if (sender != null) {
                        try {
                            sender.send(connectionDetails, message);
                        } catch (Exception e) {
                            log.error("Error sending alert via {}", connectionDetails.getType(), e);
                        }
                    } else {
                        log.warn("No sender found for message channel: {}", connectionDetails.getType());
                    }
                }
            }
        }
    }
}
