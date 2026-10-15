package tech.ebp.oqm.plugin.alertMessenger.alerts;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.WebhookClient;
import tech.ebp.oqm.plugin.alertMessenger.connections.SlackConnection;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.slack.SlackWebhookMessage;

import java.net.URI;

/**
 * Sends alerts to Slack using webhooks.
 * `docs`: https://docs.slack.dev/messaging/sending-messages-using-incoming-webhooks/
 */
@ApplicationScoped
@Slf4j
public class SlackAlertSender implements AlertSender<SlackConnection> {


    @Override
    public MessageChannels messageChannel() {
        return MessageChannels.SLACK;
    }

    @Override
    public void send(SlackConnection connection, EventNotificationWrapper eventNotificationWrapper) {
        log.info("Sending alert via Slack");
        try (WebhookClient client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(connection.getWebhookUrl()))
                .build(WebhookClient.class)) {
            client.send(new SlackWebhookMessage(eventNotificationWrapper.toString()));
        } catch (Exception e) {
            log.error("Failed to send alert via Slack", e);
        }
    }
}
