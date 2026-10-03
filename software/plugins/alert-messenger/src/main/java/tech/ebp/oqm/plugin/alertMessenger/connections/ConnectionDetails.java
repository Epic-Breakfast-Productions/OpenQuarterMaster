package tech.ebp.oqm.plugin.alertMessenger.connections;

import lombok.Getter;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

@Getter
public abstract class ConnectionDetails {
    private final MessageChannels messageChannel;

    ConnectionDetails(MessageChannels channel) {
        this.messageChannel = channel;
    }
}