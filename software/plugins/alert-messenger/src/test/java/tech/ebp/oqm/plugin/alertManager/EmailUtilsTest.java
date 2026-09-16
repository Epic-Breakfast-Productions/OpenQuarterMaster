import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.ebp.oqm.plugin.alertMessenger.utils.EmailUtils;
import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.Mail;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

// Unit tests for the EmailUtils class to verify email-sending functionality.
@QuarkusTest
public class EmailUtilsTest {

    // Mocking the Mailer class to simulate sending emails without actually sending
    // them
    @Mock
    Mailer mailer;

    // Injecting the mock Mailer into the EmailUtils class for testing
    @InjectMocks
    EmailUtils emailUtils;

    // Initialize mock objects before each test runs
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test case to verify that the sendEmail() function in EmailUtils works as
    // expected
    // Verifies that the EmailUtils class sends emails with the correct recipient, subject, and content.
    @Test
    public void testSendEmail() {

        // Arrange: Prepare sample email details
        String recipient = "${TEST_EMAIL_ADDRESS}";
        String subject = "Test Subject";
        String content = "Test Content";

        // Act: Call the sendEmail() function
        emailUtils.sendEmail(recipient, subject, content);

        // Capture the arguments passed to mailer.send() to verify that the correct data
        // is sent
        ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(mailer).send(mailCaptor.capture());

        // Assert: Verify that the email data (recipient, subject, content) matches what
        // was sent
        Mail sentMail = mailCaptor.getValue();
        assertEquals(recipient, sentMail.getTo().get(0)); // Verify recipient email address
        assertEquals(subject, sentMail.getSubject()); // Verify email subject
        assertEquals(content, sentMail.getText()); // Verify email content
    }

    // Test case for sending an actual email (disabled for now)

    // Injecting the actual EmailUtils without mocks to send a real email

    @Inject
    EmailUtils realEmailUtils; // Inject the real EmailUtils object

    /*
     * // Test case to send a real email using realEmailUtils
     * 
     * @Test
     * public void testSendRealEmail() {
     * // Arrange: Get the recipient's email address from the environment variables
     * String recipient = System.getenv("TEST_EMAIL_ADDRESS"); // Retrieve recipient
     * email from environment variable
     * String subject = "Real Test Email"; // Subject of the test email
     * String content = "This is a real email sent from the Quarkus application.";
     * // Email body content
     * 
     * // Act: Send the real email
     * realEmailUtils.sendEmail(recipient, subject, content);
     * 
     * // Output a message to the console to confirm that the email was sent
     * System.out.println("Real email sent to " + recipient);
     * }
     */

}
