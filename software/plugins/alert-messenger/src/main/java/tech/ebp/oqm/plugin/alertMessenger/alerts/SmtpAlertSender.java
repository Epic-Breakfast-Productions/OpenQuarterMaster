package tech.ebp.oqm.plugin.alertMessenger.alerts;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;

@ApplicationScoped
@Slf4j
public class SmtpAlertSender implements AlertSender {

    @Override
    public MessageChannels messageChannel() {
        return MessageChannels.EMAIL;
    }

    @Inject
    Mailer mailer;

    @Override
    public void send(ConnectionDetails connection, EventNotificationWrapper eventNotificationWrapper) {
        log.info("Sending alert to {} via SMTP", connection.getEmailDestination());
        String subject = "OQM Alert: " + eventNotificationWrapper.getEventType();

        mailer.send(Mail.withText(connection.getEmailDestination(), subject, formatEventNotification(eventNotificationWrapper)));
    }

    private static String formatEventNotification(EventNotificationWrapper eventNotificationWrapper) {
        return "Database: " + eventNotificationWrapper.getDatabase() + "\n" +
                "Event Type: " + eventNotificationWrapper.getEventType() + "\n" +
                "Object Type: " + eventNotificationWrapper.getObjectType() + "\n" +
                "Object ID: " + eventNotificationWrapper.getObjectId() + "\n" +
                "Event Data: " + (eventNotificationWrapper.getEvent() == null ? "{}" : eventNotificationWrapper.getEvent().toPrettyString());
    }
}