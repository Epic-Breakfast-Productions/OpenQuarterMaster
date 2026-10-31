package tech.ebp.oqm.plugin.alertMessenger.alerts.senders;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.WebhookClient;
import tech.ebp.oqm.plugin.alertMessenger.alerts.AlertSender;
import tech.ebp.oqm.plugin.alertMessenger.connections.DiscordConnection;
import tech.ebp.oqm.plugin.alertMessenger.throttle.EventMetrics;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannel;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord.DiscordEmbed;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord.DiscordField;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord.DiscordWebhookMessage;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

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
    public MessageChannel messageChannel() {
        return MessageChannel.DISCORD;
    }

    @Override
    public void send(DiscordConnection connection, EventNotificationWrapper event) {
        try (WebhookClient client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(this.getDecryptedWebhookUrl(connection)))
                .build(WebhookClient.class)) {
            client.send(toDiscordMessage(event));
        } catch (Exception e) {
            log.error("Failed to send Discord alert batch for user {}", connection.getUserId(), e);
            throw new IllegalStateException("Discord delivery failed", e);
        }
    }

    @Override
    public void sendMetrics(DiscordConnection connection, EventMetrics eventMetrics) {
        try (WebhookClient client = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(this.getDecryptedWebhookUrl(connection)))
            .build(WebhookClient.class)) {
            client.send(toDiscordMetrics(eventMetrics));
        } catch (Exception e) {
            log.error("Failed to send Discord alert batch for user {}", connection.getUserId(), e);
            throw new IllegalStateException("Discord delivery failed", e);
        }
    }

    private DiscordWebhookMessage toDiscordMessage(EventNotificationWrapper event) {
        return new DiscordWebhookMessage(
            "OpenQuarterMaster",
            icon,
            null,
            List.of(toDiscordEmbed(event)));
    }

    private DiscordWebhookMessage toDiscordMetrics(EventMetrics eventMetrics) {
        return new DiscordWebhookMessage(
            "OpenQuarterMaster",
            icon,
            null,
            List.of(toDiscordEmbed(eventMetrics)));
    }

    private DiscordEmbed toDiscordEmbed(EventNotificationWrapper event) {
        String eventType = event.getEventType().name();
        String objectType = event.getObjectType().name();
        String objectId = String.valueOf(event.getObjectId());
        String database = String.valueOf(event.getDatabase());

        return new DiscordEmbed(
            eventType + " · " + objectType,
            "A **" + eventType + "** event occurred in OpenQuarterMaster.\n" +
                "The **" + objectType + "** object with ID `" + objectId + "` " +
                "was affected in database `" + database + "`.",
            this.getColor(event),
            List.of(
                new DiscordField("Object type", objectType, true),
                new DiscordField("Event type", eventType, true),
                new DiscordField("Object ID", objectId, true),
                new DiscordField("Database", database, true),
                new DiscordField("Event data", this.boundedEventData(event), false)));
    }

    private DiscordEmbed toDiscordEmbed(EventMetrics eventMetrics) {
        List<DiscordField> fields = eventMetrics.getEventCountsByDatabaseId()
            .entrySet()
            .stream()
            .map(entry -> new DiscordField(
                "Database " + entry.getKey(),
                entry.getValue().entrySet()
                    .stream()
                    .map(count -> count.getKey() + ": " + count.getValue())
                    .collect(Collectors.joining("\n")),
                false))
            .toList();

        return new DiscordEmbed(
            "Event summary",
            "Event counts by database",
            this.getColor(eventMetrics.getPreferenceId()),
            fields);
    }

    private String boundedEventData(EventNotificationWrapper event) {
        String eventData = event.getEvent() == null ? "{}" : event.getEvent().toPrettyString();
        if (eventData.length() <= DISCORD_FIELD_LIMIT) {
            return eventData;
        }

        return eventData.substring(0, DISCORD_FIELD_LIMIT - TRUNCATION_SUFFIX.length()) + TRUNCATION_SUFFIX;
    }

    private int getColor(EventNotificationWrapper event) {
        String key = event.getObjectType() + ":" + event.getEventType();
        return key.hashCode() & 0xFFFFFF;
    }

    private int getColor(String preferenceId) {
        return preferenceId.hashCode() & 0xFFFFFF;
    }

    private String getDecryptedWebhookUrl(DiscordConnection connection) {
        connection.decryptCredentials();
        return connection.getWebhookUrl();
    }
}
