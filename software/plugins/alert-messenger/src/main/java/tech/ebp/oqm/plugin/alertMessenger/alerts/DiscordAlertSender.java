package tech.ebp.oqm.plugin.alertMessenger.alerts;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.WebhookClient;
import tech.ebp.oqm.plugin.alertMessenger.connections.DiscordConnection;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord.DiscordEmbed;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord.DiscordField;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord.DiscordWebhookMessage;

import java.net.URI;
import java.util.List;

/**
 * Sends alerts to Discord using webhooks.
 * `docs`: https://docs.discord.com/developers/resources/webhook
 */
@ApplicationScoped
@Slf4j
public class DiscordAlertSender implements AlertSender<DiscordConnection> {

    @ConfigProperty(name = "oqm.alert-messenger.favicon-url")
    String icon;

    @Override
    public MessageChannels messageChannel() {
        return MessageChannels.DISCORD;
    }

    @Override
    public void send(DiscordConnection connection, EventNotificationWrapper eventNotificationWrapper) {
        try (WebhookClient client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(connection.getWebhookUrl()))
                .build(WebhookClient.class)) {
            client.send(toDiscordMessage(eventNotificationWrapper));
        } catch (Exception e) {
            log.error("Error sending alert via Webhook to {}: {}", connection.getWebhookUrl(), e.getMessage(), e);
        }
    }

    private DiscordWebhookMessage toDiscordMessage(EventNotificationWrapper event) {
        String eventType = event.getEventType().name();
        String objectType = event.getObjectType().name();

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

        return new DiscordWebhookMessage(
            "OpenQuarterMaster",
            icon,
            null,
            List.of(embed));
    }

    private int getColor(EventNotificationWrapper event) {
        String key = event.getObjectType() + ":" + event.getEventType();
        return key.hashCode() & 0xFFFFFF;
    }
}
