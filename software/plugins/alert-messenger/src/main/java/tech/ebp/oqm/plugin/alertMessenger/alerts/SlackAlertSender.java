package tech.ebp.oqm.plugin.alertMessenger.alerts;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.WebhookClient;
import tech.ebp.oqm.plugin.alertMessenger.connections.SlackConnection;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.slack.SlackWebhookMessage;

/**
 * Sends alerts to Slack using webhooks.
 * `docs`: https://docs.slack.dev/messaging/sending-messages-using-incoming-webhooks/
 */
@ApplicationScoped
@Slf4j
public class SlackAlertSender implements AlertSender<SlackConnection> {

    @Inject
    @RestClient
    WebhookClient webhookClient;

    @Override
    public MessageChannels messageChannel() {
        return MessageChannels.SLACK;
    }

    @Override
    public void send(SlackConnection connection, EventNotificationWrapper eventNotificationWrapper) {
        log.info("Sending alert via Slack");
        try {
            webhookClient.send(connection.getWebhookUrl(), new SlackWebhookMessage(eventNotificationWrapper.toString()));
        } catch (Exception e) {
            log.error("Error sending alert to {} via Slack", connection.getWebhookUrl(), e);
        }
    }
}
