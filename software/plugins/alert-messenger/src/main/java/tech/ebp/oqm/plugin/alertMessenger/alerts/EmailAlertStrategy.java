package tech.ebp.oqm.plugin.alertMessenger.alerts;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;

@ApplicationScoped
public class EmailAlertStrategy implements AlertStrategy {

    @Inject
    Mailer mailer;

    @Override
    public void sendAlert(EventNotificationWrapper message, String recipient) {
        String subject = "OQM Alert: " + message.getEventType();

        mailer.send(
            Mail.withText(recipient, subject, message.toString())
        );
    }
}
