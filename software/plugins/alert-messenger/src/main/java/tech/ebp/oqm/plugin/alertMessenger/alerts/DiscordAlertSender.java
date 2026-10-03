package tech.ebp.oqm.plugin.alertMessenger.alerts;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.WebhookClient;
import tech.ebp.oqm.plugin.alertMessenger.connections.DiscordConnection;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;
import tech.ebp.oqm.plugin.alertMessenger.utils.discord.DiscordEmbed;
import tech.ebp.oqm.plugin.alertMessenger.utils.discord.DiscordField;
import tech.ebp.oqm.plugin.alertMessenger.utils.discord.DiscordWebhookMessage;

import java.util.List;

@ApplicationScoped
@Slf4j
public class DiscordAlertSender implements AlertSender<DiscordConnection> {

    @Inject
    @RestClient
    WebhookClient webhookClient;

    @Override
    public MessageChannels messageChannel() {
        return MessageChannels.DISCORD;
    }

    @Override
    public void send(DiscordConnection connection, EventNotificationWrapper eventNotificationWrapper) {
        try {
            webhookClient.send(connection.getDestination(), toDiscordMessage(eventNotificationWrapper));
        } catch (Exception e) {
            log.error("Error sending alert via Webhook to {}: {}", connection.getDestination(), e.getMessage(), e);
        }
    }

    private DiscordWebhookMessage toDiscordMessage(EventNotificationWrapper event) {
        String eventType = String.valueOf(event.getEventType());
        String objectType = String.valueOf(event.getObjectType());

        DiscordEmbed embed = new DiscordEmbed(
            eventType,
            "Event in OpenQuarterMaster",
            this.getColor(event),
            List.of(
                new DiscordField("Object type", objectType, true),
                new DiscordField("Event type", eventType, true),
                new DiscordField("Object ID", String.valueOf(event.getObjectId()), true),
                new DiscordField("Database", String.valueOf(event.getDatabase()), true),
                new DiscordField("Event data", event.getEvent() == null ? "{}" : event.getEvent().toPrettyString(), false)));

        return new DiscordWebhookMessage("OpenQuarterMaster", "https://www.linkedin.com/showcase/open-quartermaster/", null, List.of(embed));
    }

    private int getColor(EventNotificationWrapper event) {
        String key = event.getObjectType() + ":" + event.getEventType();
        return key.hashCode() & 0xFFFFFF;
    }
}
