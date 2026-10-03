package tech.ebp.oqm.plugin.alertMessenger.connections;

import lombok.Getter;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

@Getter
public class DiscordConnection extends ConnectionDetails {

    private final String destination;

    public DiscordConnection(String destination) {
        super(MessageChannels.DISCORD);
        this.destination = destination;
    }
}
