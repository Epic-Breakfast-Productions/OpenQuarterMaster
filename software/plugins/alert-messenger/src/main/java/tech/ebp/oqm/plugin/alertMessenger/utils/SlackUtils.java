package tech.ebp.oqm.plugin.alertMessenger.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SlackUtils {

    private static final Logger LOG = Logger.getLogger(SlackUtils.class);

    // Slack webhook URL
    private static final String SLACK_WEBHOOK_URL = System.getenv("SLACK_WEBHOOK_URL");

    /**
    * Method to send a message to a Slack channel using an Incoming Webhook.
    *
    * @param message The content of the message to send to Slack.
    */
    public void sendSlackMessage(String message) {
        try {
            // Create an HTTP client
            Client client = ClientBuilder.newClient();

            // Build the payload for the Slack message
            String payload = "{\"text\":\"" + message + "\"}";

            // Send the POST request to Slack webhook
            client.target(SLACK_WEBHOOK_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(payload));

            // Log success message
            LOG.info("Slack message sent: " + message);

        } catch (Exception e) {
            // Log any exceptions encountered
            LOG.error("Failed to send Slack message: " + e.getMessage(), e);
        }
    }
}