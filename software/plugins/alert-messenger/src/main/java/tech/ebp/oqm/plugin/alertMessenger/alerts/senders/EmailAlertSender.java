package tech.ebp.oqm.plugin.alertMessenger.alerts.senders;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.alerts.AlertSender;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannel;
import tech.ebp.oqm.plugin.alertMessenger.connections.EmailConnection;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class EmailAlertSender implements AlertSender<EmailConnection> {

    @Override
    public MessageChannel messageChannel() {
        return MessageChannel.EMAIL;
    }

    @Inject
    Mailer mailer;

    @Override
    public void send(EmailConnection connection, List<EventNotificationWrapper> events) {
        log.info("Sending alert to {} via SMTP", connection.getDestination());
        String subject = events.size() == 1
            ? "OQM Alert: " + events.get(0).getEventType()
            : "OQM Alerts: " + events.size() + " events";
        String text = events.stream()
            .map(EmailAlertSender::formatEventNotification)
            .collect(Collectors.joining("\n\n"));

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
