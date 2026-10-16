package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

@Getter
@Setter
@BsonDiscriminator(value = "DISCORD")
@JsonTypeName("DISCORD")
public class DiscordConnection extends ConnectionDetails {

    private String webhookUrl;

    public DiscordConnection() {
        super(MessageChannels.DISCORD);
    }

    public DiscordConnection(String webhookUrl) {
        this();
        this.webhookUrl = webhookUrl;
    }
}
