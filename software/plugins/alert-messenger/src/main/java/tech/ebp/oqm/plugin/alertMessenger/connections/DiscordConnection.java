package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.utils.EncryptedString;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

@Getter
@Setter
@BsonDiscriminator(value = "DISCORD")
@JsonTypeName("DISCORD")
public class DiscordConnection extends ConnectionDetails {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private EncryptedString webhookUrl;

    private String name;

    public DiscordConnection() {
        super(MessageChannels.DISCORD);
    }

    public DiscordConnection(EncryptedString webhookUrl) {
        this();
        this.webhookUrl = webhookUrl;
    }
}
