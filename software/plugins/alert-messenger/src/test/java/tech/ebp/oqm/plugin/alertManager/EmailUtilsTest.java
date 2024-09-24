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

@QuarkusTest
public class EmailUtilsTest {
    
    @Mock
    Mailer mailer;

    @InjectMocks
    EmailUtils emailUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendEmail() {
        // Arrange
        String recipient = "${TEST_EMAIL_ADDRESS}";
        String subject = "Test Subject";
        String content = "Test Content";

        // Act
        emailUtils.sendEmail(recipient, subject, content);

        // Capture the arguments passed to mailer.send()
        ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(mailer).send(mailCaptor.capture());

        // Assert the captured Mail object
        Mail sentMail = mailCaptor.getValue();
        assertEquals(recipient, sentMail.getTo().get(0));
        assertEquals(subject, sentMail.getSubject());
        assertEquals(content, sentMail.getText());
    }

    /*

    // New test for sending an actual email (No @Mock or @InjectMocks)
    @Inject
    EmailUtils realEmailUtils;  // Inject the real EmailUtils

    // New test for sending an actual email
    @Test
    public void testSendRealEmail() {
        // Arrange: Use your personal email as an environment variable here
        String recipient = System.getenv("TEST_EMAIL_ADDRESS");
        String subject = "Real Test Email";
        String content = "This is a real email sent from the Quarkus application.";
    
        // Act: Send real email
        realEmailUtils.sendEmail(recipient, subject, content);
    
        // Output a message to confirm email was sent
        System.out.println("Real email sent to " + recipient);
    } */
    
}
