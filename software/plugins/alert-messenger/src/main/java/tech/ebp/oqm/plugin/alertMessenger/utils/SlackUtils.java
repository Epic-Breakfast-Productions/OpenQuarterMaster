package tech.ebp.oqm.plugin.alertMessenger.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserPreferencesRepository;
import tech.ebp.oqm.plugin.alertMessenger.model.UserPreferences;

import java.util.Optional;
import java.util.UUID;

// Utility class for sending Slack messages using a webhook configured for each user.
@ApplicationScoped
public class SlackUtils {

    private static final Logger LOG = Logger.getLogger(SlackUtils.class);

    @Inject
    UserPreferencesRepository userPreferencesRepository;

    /**
     * Sends a message to a Slack channel using an Incoming Webhook.
     *
     * @param userId  The UUID of the user whose Slack webhook is being used.
     * @param message The content of the message to send to Slack.
     */
    public void sendSlackMessage(UUID userId, String message) {
        try {
            // Retrieve the Slack webhook URL from the user_preferences table
            Optional<UserPreferences> preferencesOptional = userPreferencesRepository.findByIdOptional(userId);
            if (preferencesOptional.isEmpty()) {
                LOG.warn("No preferences found for user ID: " + userId);
                return;
            }

            String slackWebhookUrl = preferencesOptional.get().getSlackWebhook();
            if (slackWebhookUrl == null || slackWebhookUrl.isEmpty()) {
                LOG.warn("Slack webhook URL is not configured for user ID: " + userId);
                return;
            }

            // Create an HTTP client
            Client client = ClientBuilder.newClient();

            // Build the payload for the Slack message
            String payload = "{\"text\":\"" + message + "\"}";

            // Send the POST request to Slack webhook
            client.target(slackWebhookUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(payload));

            // Log success message
            LOG.info("Slack message sent to user ID " + userId + ": " + message);

        } catch (Exception e) {
            // Log any exceptions encountered
            LOG.error("Failed to send Slack message for user ID " + userId + ": " + e.getMessage(), e);
        }
    }
}
