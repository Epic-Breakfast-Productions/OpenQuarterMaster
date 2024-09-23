import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class BasicTest {

    @Test
    public void testBasicFunctionality() {
        assertTrue(true);  // Simple test to verify setup
    }
}

/*import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import tech.ebp.oqm.plugin.alertMessenger.utils.EmailUtils;

@QuarkusTest
public class EmailUtilsTest {

    @InjectMock
    Mailer mailer;  // Mocked mailer

    @Inject
    EmailUtils emailUtils;  // Actual EmailUtils instance

    @Test
    public void testSendEmail() {
        // Arrange
        String recipient = "andrebp21@juniata.edu";
        String subject = "Test Subject";
        String content = "Test Content";

        // Act
        emailUtils.sendEmail(recipient, subject, content);

        // Assert that the emailUtils and mailer are properly injected and used
        assertNotNull(emailUtils);  // EmailUtils should be injected
        assertNotNull(mailer);      // Mailer should be mocked and injected
    }
}
*/