package tech.ebp.oqm.plugin.alertMessenger;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import tech.ebp.oqm.plugin.alertMessenger.model.UserPreferences;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserPreferencesRepository;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserRepository;
import tech.ebp.oqm.plugin.alertMessenger.utils.EmailUtils;
import tech.ebp.oqm.plugin.alertMessenger.utils.SlackUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;


// AlertConsumer processes Kafka messages from the `oqm-core-all-events` topic 
// and sends alerts via email and Slack based on user preferences.
@Slf4j
@ApplicationScoped
public class AlertConsumer {

    @Inject
    private UserPreferencesRepository userPreferencesRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private EmailUtils email;

    @Inject
    private SlackUtils slack;

    // Initializes the AlertConsumer bean and logs startup information.
    @PostConstruct
    public void init() {
        log.info("Starting AlertConsumer");
    }

    // Handles incoming Kafka messages, processes their payload, 
    // and routes alerts based on user preferences.
    @Incoming("oqm-core-all-events")
    public CompletionStage<Void> receive(Message<ObjectNode> message) {
        try {
            ObjectNode payload = message.getPayload();
            log.info("Received alert message: {}", payload);

            processMessage(payload);

            return message.ack();
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            return message.nack(e);
        }
    }

    // Validates the payload structure, retrieves user preferences, 
    // and decides whether to send email or Slack notifications.
    private void processMessage(ObjectNode payload) {
        if (!payload.has("type") || !payload.has("details") || !payload.has("userId")) {
            log.error("Invalid payload: {}", payload);
            return;
        }

        String alertType = payload.get("type").asText();
        String alertDetails = payload.get("details").asText();
        UUID userId = UUID.fromString(payload.get("userId").asText());

        Optional<UserPreferences> preferencesOptional = userPreferencesRepository.findByIdOptional(userId);
        if (preferencesOptional.isEmpty()) {
            log.warn("No user preferences found for user ID: {}", userId);
            return;
        }

        UserPreferences preferences = preferencesOptional.get();

        if ("yes".equalsIgnoreCase(preferences.getEmailNotifications())) {
            handleEmailNotification(userId, alertType, alertDetails);
        } else {
            log.info("Email notifications not enabled for user ID: {}", userId);
        }

        if (preferences.getSlackWebhook() != null && !preferences.getSlackWebhook().isEmpty()) {
            handleSlackNotification(userId, alertType, alertDetails);
        } else {
            log.info("Slack notifications not enabled for user ID: {}", userId);
        }
    }

    // Sends email notifications using the EmailUtils class if the user's email is configured.
    private void handleEmailNotification(UUID userId, String alertType, String alertDetails) {
        var userInfo = userRepository.findById(userId);
        if (userInfo == null || userInfo.getEmail() == null) {
            log.warn("No email found for user ID: {}", userId);
            return;
        }
        email.sendEmail(userInfo.getEmail(), alertType, alertDetails);
        log.info("Email sent to user ID {}: {}", userId, userInfo.getEmail());
    }

    // Sends Slack notifications using the SlackUtils class if the user has a Slack webhook configured.
    private void handleSlackNotification(UUID userId, String alertType, String alertDetails) {
        slack.sendSlackMessage(userId, String.format("%s: \n%s", alertType, alertDetails));
        log.info("Slack message sent to user ID {}", userId);
    }

    // Setter methods for testing
    public void setUserPreferencesRepository(UserPreferencesRepository userPreferencesRepository) {
        this.userPreferencesRepository = userPreferencesRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setEmailUtils(EmailUtils email) {
        this.email = email;
    }

    public void setSlackUtils(SlackUtils slack) {
        this.slack = slack;
    }
}
