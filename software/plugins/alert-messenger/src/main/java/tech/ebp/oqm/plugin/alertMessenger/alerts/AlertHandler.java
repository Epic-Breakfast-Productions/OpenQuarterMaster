package tech.ebp.oqm.plugin.alertMessenger.alerts;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferences;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferencesService;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

import java.util.Iterator;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class AlertHandler {
    private final Map<MessageChannels, AlertStrategy> strategies;

    @Inject
    UserPreferencesService userPreferencesService;

    public AlertHandler() {
        this.strategies = Map.of(
            MessageChannels.EMAIL, new EmailAlertStrategy()
        );
    }

    public void handleAlert(EventNotificationWrapper message) {
        Iterator<UserPreferences> iterator = userPreferencesService.getIterator();
        while (iterator.hasNext()) {
            UserPreferences preferences = iterator.next();
            if(preferences.objectTypes.contains(message.getObjectType()) || preferences.eventTypes.contains(message.getEventType())) {
                for (Map.Entry<MessageChannels, String> entry : preferences.messageChannels.entrySet()) {
                    MessageChannels channel = entry.getKey();
                    if(strategies.containsKey(channel)) {
                        AlertStrategy strategy = strategies.get(channel);
                        strategy.sendAlert(message);
                    } else {
                        log.warn("No strategy found for channel: {}", channel);
                    }
                }
            }
        }
    }
}
