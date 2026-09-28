package tech.ebp.oqm.plugin.alertMessenger.alerts;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferences;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferencesService;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannelRecipient;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

import java.util.Iterator;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class AlertHandler {
    private Map<MessageChannels, AlertStrategy> strategies;

    @Inject
    UserPreferencesService userPreferencesService;

    @Inject
    EmailAlertStrategy emailAlertStrategy;

    @PostConstruct
    void initializeStrategies() {
        this.strategies = Map.of(
            MessageChannels.EMAIL, emailAlertStrategy
        );
    }

    public void handleAlert(EventNotificationWrapper message) {
        Iterator<UserPreferences> iterator = userPreferencesService.getIterator();
        while (iterator.hasNext()) {
            UserPreferences preferences = iterator.next();
            if(preferences.objectTypes.contains(message.getObjectType()) || preferences.eventTypes.contains(message.getEventType())) {
                for(MessageChannelRecipient recipient : preferences.messageChannels) {
                    if(strategies.containsKey(recipient.channel())) {
                        try {
                            strategies.get(recipient.channel()).sendAlert(message, recipient.destination());
                        } catch (Exception e) {
                            log.error("Error sending alert to {} via {}. Due to {}", recipient.destination(), recipient.channel(), e.getMessage());
                        }
                    } else {
                        log.warn("No strategy found for channel: {}", recipient.channel());
                    }
                }
            }
        }
    }
}
