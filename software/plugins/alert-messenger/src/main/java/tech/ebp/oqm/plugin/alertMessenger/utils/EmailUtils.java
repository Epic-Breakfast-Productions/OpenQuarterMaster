package tech.ebp.oqm.plugin.alertMessenger.utils;

import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.Mail;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailUtils {

    @Inject
    Mailer mailer;

    public void sendEmail(String recipient, String subject, String content) {
        mailer.send(Mail.withText(recipient, subject, content));
    }
}