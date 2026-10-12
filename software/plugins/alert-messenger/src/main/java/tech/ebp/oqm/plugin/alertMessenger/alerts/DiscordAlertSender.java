package tech.ebp.oqm.plugin.alertMessenger.alerts;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.WebhookClient;
import tech.ebp.oqm.plugin.alertMessenger.connections.DiscordConnection;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord.DiscordEmbed;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord.DiscordField;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord.DiscordWebhookMessage;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Sends alerts to Discord using webhooks.
 * `docs`: https://docs.discord.com/developers/resources/webhook
*/
@ApplicationScoped
@Slf4j
public class DiscordAlertSender implements AlertSender<DiscordConnection> {

    private static final String ICON = "https://media.licdn.com/dms/image/v2/D4E0BAQEhoks7gzObxg/company-logo_200_200/company-logo_200_200/0/1724644030028/open_quartermaster_logo?e=1792627200&v=beta&t=UtzCaRMgxO25D9jmmipqWTsyFpS3wlV5iuMIECjR2UA";

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
            webhookClient.send(connection.getWebhookUrl(), toDiscordMessage(eventNotificationWrapper));
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

        return new DiscordWebhookMessage("OpenQuarterMaster", ICON, null, List.of(embed));
    }

    private int getColor(EventNotificationWrapper event) {
        String key = event.getObjectType() + ":" + event.getEventType();
        return key.hashCode() & 0xFFFFFF;
    }
}
