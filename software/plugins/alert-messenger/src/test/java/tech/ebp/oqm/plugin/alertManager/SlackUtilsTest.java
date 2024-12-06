/* You have to have a user (with a slack webhook configured) in your database with the environment variable TEST_USER_ID set to that user's slack channel to test this. */
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.alertMessenger.utils.SlackUtils;
import jakarta.inject.Inject;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@QuarkusTest
public class SlackUtilsTest {

    // Uncomment this to run a test on the slack Utilities.
    
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
            slackUtils.sendSlackMessage(UUID.fromString("${TEST_USER_ID}"), message);
        });
    }
}