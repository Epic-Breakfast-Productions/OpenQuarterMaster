package tech.ebp.oqm.plugin.alertMessenger.alerts;

import io.quarkus.arc.All;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetailsService;
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

    @Inject
    ConnectionDetailsService connectionDetailsService;

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

    //make it non-blocking
    public void handleAlert(EventNotificationWrapper message) {
        RuntimeException deliveryFailure = null;
        Iterator<UserPreferences> iterator = userPreferencesService.getIterator();
        while (iterator.hasNext()) {
            UserPreferences preferences = iterator.next();
            if ((preferences.objectTypes != null && preferences.objectTypes.contains(message.getObjectType())) || (preferences.eventTypes != null && preferences.eventTypes.contains(message.getEventType()))) {
                for (ConnectionDetails connectionDetails : userPreferencesService.getConnectionDetails(preferences)) {
                    log.debug("Connection details: {}", connectionDetails);
                    AlertSender sender = alertSenders.get(connectionDetails.getType());
                    if (sender != null) {
                        try {
                            sender.send(connectionDetails, message);
                            connectionDetailsService.updateLastError(connectionDetails, null);
                        } catch (Exception e) {
                            String error = sanitizeError(e);
                            connectionDetailsService.updateLastError(connectionDetails, error);
                            log.error("Error sending alert via {}: {}", connectionDetails.getType(), error);
                            if (deliveryFailure == null) {
                                deliveryFailure = new IllegalStateException("One or more alert deliveries failed");
                            }
                        }
                    } else {
                        log.warn("No sender found for message channel: {}", connectionDetails.getType());
                    }
                }
            }
        }
        if (deliveryFailure != null) {
            throw deliveryFailure;
        }
    }

    private String sanitizeError(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
