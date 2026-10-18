package tech.ebp.oqm.plugin.alertMessenger.alerts.senders;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.alerts.AlertSender;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;
import tech.ebp.oqm.plugin.alertMessenger.connections.EmailConnection;

import java.util.Collection;
import java.util.function.Consumer;

@ApplicationScoped
@Slf4j
public class EmailAlertSender implements AlertSender<EmailConnection> {

    @Override
    public MessageChannels messageChannel() {
        return MessageChannels.EMAIL;
    }

    @Inject
    Mailer mailer;

    @Override
    public void send(EmailConnection connection, EventNotificationWrapper eventNotificationWrapper) {
        log.info("Sending alert to {} via SMTP", connection.getDestination());
        String subject = "OQM Alert: " + eventNotificationWrapper.getEventType();
        String text = formatEventNotification(eventNotificationWrapper);

        Mail mail = Mail.withText(connection.getDestination(), subject, text);
        addRecipients(connection.getCc(), mail::addCc);
        addRecipients(connection.getBcc(), mail::addBcc);

        mailer.send(mail);
    }

    private static void addRecipients(Collection<String> recipients, Consumer<String[]> addRecipients) {
        if (recipients != null && !recipients.isEmpty()) {
            addRecipients.accept(recipients.toArray(String[]::new));
        }
    }

    private static String formatEventNotification(EventNotificationWrapper eventNotificationWrapper) {
        return "Database: " + eventNotificationWrapper.getDatabase() + "\n" +
                "Event Type: " + eventNotificationWrapper.getEventType() + "\n" +
                "Object Type: " + eventNotificationWrapper.getObjectType() + "\n" +
                "Object ID: " + eventNotificationWrapper.getObjectId() + "\n" +
                "Event Data: " + (eventNotificationWrapper.getEvent() == null ? "{}" : eventNotificationWrapper.getEvent().toPrettyString());
    }
}
