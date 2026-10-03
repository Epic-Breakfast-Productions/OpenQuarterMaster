package tech.ebp.oqm.plugin.alertMessenger.alerts;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferences;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferencesService;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@ApplicationScoped
public class AlertHandler {

    @Inject
    Instance<AlertSender> senderInstances;

    @Inject
    UserPreferencesService userPreferencesService;

    private Map<MessageChannels, AlertSender> alertSenders;

    @PostConstruct
    void initializeSenders() {
        alertSenders = StreamSupport
            .stream(senderInstances.spliterator(), false)
            .collect(Collectors.toUnmodifiableMap(
                AlertSender::messageChannel,
                Function.identity()
            ));
    }


    public void handleAlert(EventNotificationWrapper message) {
        Iterator<UserPreferences> iterator = userPreferencesService.getIterator();
        while (iterator.hasNext()) {
            UserPreferences preferences = iterator.next();
            if (preferences.objectTypes.contains(message.getObjectType()) || preferences.eventTypes.contains(message.getEventType())) {
                for (ConnectionDetails connectionDetails : preferences.getConnectionDetails()) {
                    AlertSender sender = alertSenders.get(connectionDetails.getMessageChannel());
                    if (sender != null) {
                        try {
                            sender.send(connectionDetails, message);
                        } catch (Exception e) {
                            log.error("Error sending alert via {}", connectionDetails.getMessageChannel(), e);
                        }
                    } else {
                        log.warn("No sender found for message channel: {}", connectionDetails.getMessageChannel());
                    }
                }
            }
        }
    }
}
