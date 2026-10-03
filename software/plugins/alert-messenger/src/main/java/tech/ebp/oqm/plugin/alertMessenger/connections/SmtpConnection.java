package tech.ebp.oqm.plugin.alertMessenger.connections;

import lombok.Getter;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

@Getter
public class SmtpConnection extends ConnectionDetails {

    private final String destination;

    public SmtpConnection(String destination) {
        super(MessageChannels.EMAIL);
        this.destination = destination;
    }
}
