package tech.ebp.oqm.plugin.alertManager;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.ebp.oqm.plugin.alertMessenger.model.UserInfo;
import tech.ebp.oqm.plugin.alertMessenger.model.UserPreferences;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserPreferencesRepository;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserRepository;
import tech.ebp.oqm.plugin.alertMessenger.utils.EmailUtils;
import tech.ebp.oqm.plugin.alertMessenger.utils.SlackUtils;
import tech.ebp.oqm.plugin.alertMessenger.AlertConsumer;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

// Unit tests for the AlertConsumer class to verify message handling and notification logic.
@QuarkusTest
public class AlertConsumerTest {

    @Inject
    AlertConsumer alertConsumer;

    private UserPreferencesRepository userPreferencesRepositoryMock;
    private UserRepository userRepositoryMock;
    private EmailUtils emailUtilsMock;
    private SlackUtils slackUtilsMock;

    private UUID testUserId;

    @BeforeEach
    public void setup() {
        // Create mocks
        userPreferencesRepositoryMock = Mockito.mock(UserPreferencesRepository.class);
        userRepositoryMock = Mockito.mock(UserRepository.class);
        emailUtilsMock = Mockito.mock(EmailUtils.class);
        slackUtilsMock = Mockito.mock(SlackUtils.class);

        // Inject mocks using setters
        alertConsumer.setUserPreferencesRepository(userPreferencesRepositoryMock);
        alertConsumer.setUserRepository(userRepositoryMock);
        alertConsumer.setEmailUtils(emailUtilsMock);
        alertConsumer.setSlackUtils(slackUtilsMock);

        // Test data
        testUserId = UUID.randomUUID();
    }

    // Verifies that the consumer sends both email and Slack notifications for valid messages.
    @Test
    public void testReceiveMessageWithEmailAndSlackNotifications() throws Exception {
        // Mock user preferences
        UserPreferences preferences = new UserPreferences();
        preferences.setId(testUserId);
        preferences.setEmailNotifications("yes");
        preferences.setSlackWebhook("https://hooks.slack.com/services/test/webhook");
        when(userPreferencesRepositoryMock.findByIdOptional(testUserId)).thenReturn(Optional.of(preferences));

        // Mock user info
        UserInfo userInfo = new UserInfo();
        userInfo.setId(testUserId);
        userInfo.setEmail("testuser@example.com");
        when(userRepositoryMock.findById(testUserId)).thenReturn(userInfo);

        // Create a mock Kafka message
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("type", "ALERT");
        payload.put("details", "Inventory low");
        payload.put("userId", testUserId.toString());
        @SuppressWarnings("unchecked")
        Message<ObjectNode> message = (Message<ObjectNode>) mock(Message.class); // Explicit cast
        when(message.getPayload()).thenReturn(payload);

        // Call the receive method
        alertConsumer.receive(message);

        // Verify email and Slack were called
        verify(emailUtilsMock).sendEmail("testuser@example.com", "ALERT", "Inventory low");
        verify(slackUtilsMock).sendSlackMessage(testUserId, "ALERT: \nInventory low");
        verify(message).ack();
    }
}