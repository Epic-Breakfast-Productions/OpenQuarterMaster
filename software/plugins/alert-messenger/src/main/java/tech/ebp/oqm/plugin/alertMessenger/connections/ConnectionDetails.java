package tech.ebp.oqm.plugin.alertMessenger.connections;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

@Getter
@Setter
@NoArgsConstructor
public class ConnectionDetails {
    private MessageChannels messageChannel;

    // EMAIL
    private String emailDestination;

    // DISCORD / SLACK
    private String webhookUrl;
}
