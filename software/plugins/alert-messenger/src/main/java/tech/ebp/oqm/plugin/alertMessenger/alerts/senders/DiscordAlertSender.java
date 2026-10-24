package tech.ebp.oqm.plugin.alertMessenger.alerts.senders;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.WebhookClient;
import tech.ebp.oqm.plugin.alertMessenger.alerts.AlertSender;
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
    private static final int DISCORD_FIELD_LIMIT = 1_000;
    private static final String TRUNCATION_SUFFIX = "\n...[truncated]";

    @ConfigProperty(name = "oqm.alert-messenger.favicon-url")
    String icon;

    @Override
    public MessageChannels messageChannel() {
        return MessageChannels.DISCORD;
    }

    @Override
    public void send(DiscordConnection connection, List<EventNotificationWrapper> events) {
        try (WebhookClient client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(connection.getWebhookUrl().value()))
                .build(WebhookClient.class)) {
            client.send(toDiscordMessage(events));
        } catch (Exception e) {
            log.error("Failed to send Discord alert batch for user {}. exception: {}", connection.getUserId(), e);
            throw new IllegalStateException("Discord delivery failed", e);
        }
    }

    private DiscordWebhookMessage toDiscordMessage(List<EventNotificationWrapper> events) {
        return new DiscordWebhookMessage(
            "OpenQuarterMaster",
            icon,
            events.size() == 1 ? null : "OpenQuarterMaster alerts (" + events.size() + ")",
            events.stream().map(this::toDiscordEmbed).toList());
    }

    private DiscordEmbed toDiscordEmbed(EventNotificationWrapper event) {
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
                new DiscordField("Event data", this.boundedEventData(event), false)));

        return embed;
    }

    private String boundedEventData(EventNotificationWrapper event) {
        String eventData = event.getEvent().toPrettyString();
        if (eventData.length() <= DISCORD_FIELD_LIMIT) {
            return eventData;
        }

        return eventData.substring(0, DISCORD_FIELD_LIMIT - TRUNCATION_SUFFIX.length()) + TRUNCATION_SUFFIX;
    }

    private int getColor(EventNotificationWrapper event) {
        String key = event.getObjectType() + ":" + event.getEventType();
        return key.hashCode() & 0xFFFFFF;
    }
}
