package tech.ebp.oqm.plugin.alertMessenger;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import tech.ebp.oqm.plugin.alertMessenger.model.UserInfo;
import tech.ebp.oqm.plugin.alertMessenger.model.UserPreferences;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserPreferencesRepository;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserRepository;
import tech.ebp.oqm.plugin.alertMessenger.utils.EmailUtils;
import tech.ebp.oqm.plugin.alertMessenger.utils.SlackUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Slf4j
@ApplicationScoped
public class AlertConsumer {

    @Inject
    EmailUtils email;

    @Inject
    SlackUtils slack;

    @Inject
    UserPreferencesRepository userPreferencesRepository;

    @Inject
    UserRepository userRepository;

    @PostConstruct
    public void init() {
        log.info("Starting AlertConsumer");
    }

    /**
     * Receives messages from the Kafka topic "alert-messages".
     *
     * @param message the incoming Kafka message wrapped in a Message object.
     * @return a CompletionStage that acknowledges the message.
     */
    @Incoming("oqm-core-all-events")
    public CompletionStage<Void> receive(Message<ObjectNode> message) {
        try {
            // Log the message payload
            ObjectNode payload = message.getPayload();
            log.info("Received alert message: {}", payload);

            // Process the message
            processMessage(payload);

            // Acknowledge the message
            return message.ack();
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            // Nack the message to handle the failure strategy
            return message.nack(e);
        }
    }

    /**
     * Processes the payload of the incoming Kafka message.
     *
     * @param payload the JSON payload of the Kafka message.
     */
    private void processMessage(ObjectNode payload) {
        // Validate payload fields
        if (payload == null || !payload.has("type") || !payload.has("details") || !payload.has("userId")) {
            log.error("Invalid payload: {}", payload);
            return;
        }

        // Extract fields from payload
        String alertType = payload.get("type").asText();
        String alertDetails = payload.get("details").asText();
        UUID userId = UUID.fromString(payload.get("userId").asText());
        log.info("Processing alert of type: {} with details: {}", alertType, alertDetails);

        // Fetch user preferences
        Optional<UserPreferences> preferencesOptional = userPreferencesRepository.findByIdOptional(userId);
        if (preferencesOptional.isEmpty()) {
            log.warn("No user preferences found for user ID: {}", userId);
            return;
        }
        UserPreferences preferences = preferencesOptional.get();

        // Handle email notifications
        if ("yes".equalsIgnoreCase(preferences.getEmailNotifications())) {
            log.info("Email is configured. Sending email notification.");

            // Fetch the user's email address from the database
            UserInfo userInfo = userRepository.findById(userId);
            if (userInfo == null || userInfo.getEmail() == null) {
                log.warn("No email address found for user ID: {}", userId);
                return;
            }

            email.sendEmail(userInfo.getEmail(), alertType, alertDetails);
        } else {
            log.warn("Email notifications are not configured for user ID: {}", userId);
        }

        // Handle Slack notifications
        if (preferences.getSlackWebhook() != null && !preferences.getSlackWebhook().isEmpty()) {
            log.info("Slack is configured. Sending Slack message.");
            slack.sendSlackMessage(userId, String.format("%s: \n%s", alertType, alertDetails));
        } else {
            log.warn("Slack notifications are not configured for user ID: {}", userId);
        }
    }
}
