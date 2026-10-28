package tech.ebp.oqm.plugin.alertMessenger.alerts.senders;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.WebhookClient;
import tech.ebp.oqm.plugin.alertMessenger.alerts.AlertSender;
import tech.ebp.oqm.plugin.alertMessenger.connections.SlackConnection;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannel;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.slack.SlackWebhookMessage;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sends alerts to Slack using webhooks.
 * `docs`: https://docs.slack.dev/messaging/sending-messages-using-incoming-webhooks/
 */
@ApplicationScoped
@Slf4j
public class SlackAlertSender implements AlertSender<SlackConnection> {

    @Override
    public MessageChannel messageChannel() {
        return MessageChannel.SLACK;
    }

    @Override
    public void send(SlackConnection connection, List<EventNotificationWrapper> events) {
        log.info("Sending alert via Slack");
        try (WebhookClient client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(this.getDecryptedWebhookUrl(connection)))
                .build(WebhookClient.class)) {
            String text = events.stream()
                .map(EventNotificationWrapper::toString)
                .collect(Collectors.joining("\n\n"));
            client.send(new SlackWebhookMessage(text));
        } catch (Exception e) {
            log.error("Failed to send Slack alert batch for user {}", connection.getUserId(), e);
            throw new IllegalStateException("Slack delivery failed", e);
        }
    }

    private String getDecryptedWebhookUrl(SlackConnection connection) {
        connection.decryptCredentials();
        return connection.getWebhookUrl();
    }
}
