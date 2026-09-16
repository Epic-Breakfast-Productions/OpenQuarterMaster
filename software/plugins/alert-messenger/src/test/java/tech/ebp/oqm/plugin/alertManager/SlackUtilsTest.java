/* You have to have a user (with a slack webhook configured) in your database with the environment variable TEST_USER_ID set to that user's slack channel to test this. */
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.alertMessenger.utils.SlackUtils;
import jakarta.inject.Inject;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

// Unit tests for the SlackUtils class to verify message-sending functionality.
@QuarkusTest
public class SlackUtilsTest {

    // Uncomment this to run a test on the slack Utilities.
    
    // Inject the real SlackUtils class
    @Inject
    SlackUtils slackUtils;

    // Test case to verify that the sendSlackMessage() method works with real implementation
    @Test
    public void testSendSlackMessage() {
        String testUserIdEnv = System.getenv("TEST_USER_ID");
        System.out.println("TEST_USER_ID = " + testUserIdEnv); // Debugging output
    
        if (testUserIdEnv == null || testUserIdEnv.isEmpty()) {
            throw new IllegalStateException("Environment variable TEST_USER_ID is not set or is empty.");
        }
    
        UUID testUserId = UUID.fromString(testUserIdEnv);
        String message = "Test Slack Message";
    
        assertDoesNotThrow(() -> {
            slackUtils.sendSlackMessage(testUserId, message);
        });
    }
}