import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.alertMessenger.utils.SlackUtils;
import jakarta.inject.Inject;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@QuarkusTest
public class SlackUtilsTest {

    // Uncomment this to run a test on the slack Utilities.
    /*
    // Inject the real SlackUtils class
    @Inject
    SlackUtils slackUtils;

    // Test case to verify that the sendSlackMessage() method works with real implementation
    @Test
    public void testSendSlackMessage() {
        // Arrange: Prepare a sample message
        String message = "Test Slack Message";

        // Act and Assert: Ensure that the method doesn't throw an exception when sending a real message
        assertDoesNotThrow(() -> {
            slackUtils.sendSlackMessage(message);
        });
    }

    // Test case to simulate and verify error handling with real SlackUtils
    @Test
    public void testSendSlackMessageWithError() {
        // Arrange: Manually set an invalid Slack Webhook URL in the environment
        System.setProperty("SLACK_WEBHOOK_URL", "https://invalid-url");

        // Act and Assert: Ensure that sending a message to an invalid URL is handled gracefully
        assertDoesNotThrow(() -> {
            slackUtils.sendSlackMessage("Error Test Message");
        });

        // Reset the system property to avoid side effects for other tests
        System.clearProperty("SLACK_WEBHOOK_URL");
    }*/
}