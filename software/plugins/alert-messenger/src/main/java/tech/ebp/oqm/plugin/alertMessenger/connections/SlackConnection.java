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
@BsonDiscriminator(value = "SLACK")
@JsonTypeName("SLACK")
public class SlackConnection extends ConnectionDetails {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private EncryptedString webhookUrl;

    public SlackConnection() {
        super(MessageChannels.SLACK);
    }

    public SlackConnection(EncryptedString webhookUrl) {
        this();
        this.webhookUrl = webhookUrl;
    }
}
