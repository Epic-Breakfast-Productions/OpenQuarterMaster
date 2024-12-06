package tech.ebp.oqm.plugin.alertMessenger.utils;

import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.Mail;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// Utility class for sending email notifications using the Quarkus Mailer API.
@ApplicationScoped
public class EmailUtils {

    // Injecting the Mailer instance provided by Quarkus to handle sending emails
    @Inject
    Mailer mailer;

    /**
    * Method to send an email using the Quarkus Mailer.
    *
    * @param recipient The email address of the recipient.
    * @param subject   The subject of the email.
    * @param content   The body content of the email.
    */
    public void sendEmail(String recipient, String subject, String content) {
        // Create and send an email with the given recipient, subject, and content
        mailer.send(Mail.withText(recipient, subject, content));
    }
}