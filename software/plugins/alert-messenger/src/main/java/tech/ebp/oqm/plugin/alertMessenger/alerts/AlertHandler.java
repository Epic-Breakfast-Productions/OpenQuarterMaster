package tech.ebp.oqm.plugin.alertMessenger.alerts;

import io.quarkus.arc.All;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.HistoryEventFilter;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetailsService;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferences;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferencesService;
import tech.ebp.oqm.plugin.alertMessenger.throttle.ThrottleService;
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

    @Inject
    ThrottleService throttleFacade;

    @PostConstruct
    void initializeSenders() {
        alertSenders = senderInstances.stream()
            .collect(Collectors.toUnmodifiableMap(
                AlertSender::messageChannel,
                Function.identity()
            ));
        log.debug("Initialized alert senders: {}", alertSenders.keySet());
    }

    private static boolean filterEvent(EventNotificationWrapper message, UserPreferences preferences) {
        return HistoryEventFilter.filter(message, HistoryEventFilter.FilterOptions.builder()
            .objectType(preferences.objectTypes)
            .eventType(preferences.eventTypes)
            .build()) && preferences.enabled;
    }

    private static boolean filterDatabaseId(String messageDatabaseId, String preferencesDatabaseId) {
        return preferencesDatabaseId == null || preferencesDatabaseId.isBlank() || preferencesDatabaseId.equals(messageDatabaseId);
    }

    public void sendAlert(EventNotificationWrapper message) {
        ObjectId alertEventId = throttleFacade.saveAlertEvent(message);
        Iterator<UserPreferences> iterator = userPreferencesService.getIterator();
        while (iterator.hasNext()) {
            UserPreferences preferences = iterator.next();
            if (filterEvent(message, preferences) && preferences.enabled && filterDatabaseId(message.getDatabase(), preferences.databaseId)) {
                if (preferences.throttleTimeInMinutes == null || preferences.throttleTimeInMinutes == 0) {
                    for (ConnectionDetails connectionDetails : userPreferencesService.getConnectionDetails(preferences)) {
                        log.debug("Connection details type: {}", connectionDetails.getType());
                        this.sendAlert(connectionDetails, message);
                    }
                } else {
                    throttleFacade.throttle(preferences, alertEventId);
                        log.debug("Throttled connection details: {}", preferences);
                }
            }
        }
    }

    public void sendAlert(ConnectionDetails connectionDetails, List<EventNotificationWrapper> events) {
        AlertSender sender = alertSenders.get(connectionDetails.getType());
        if (sender != null) {
            try {
                sender.send(connectionDetails, events);
            } catch (Exception e) {
                String error = this.sanitizeError(e);
                connectionDetailsService.updateLastError(connectionDetails, error);
                log.error("Error sending alert via {}: {}", connectionDetails.getType(), error);
            }
        } else {
            log.warn("No sender found for message channel: {}", connectionDetails.getType());
        }
    }

    private void sendAlert(ConnectionDetails connectionDetails, EventNotificationWrapper event) {
        this.sendAlert(connectionDetails, List.of(event));
    }

    private String sanitizeError(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
