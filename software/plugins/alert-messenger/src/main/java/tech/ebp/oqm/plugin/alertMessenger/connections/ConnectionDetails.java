package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

@Getter
@Setter
@BsonDiscriminator(key = "type")
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SmtpConnection.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = DiscordConnection.class, name = "DISCORD")
})
public abstract class ConnectionDetails {
    private MessageChannels messageChannel;

    ConnectionDetails(MessageChannels channel) {
        this.messageChannel = channel;
    }
}
