package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

@Getter
@Setter
@BsonDiscriminator(value = "EMAIL")
@NoArgsConstructor
@JsonTypeName("EMAIL")
public class SmtpConnection extends ConnectionDetails {

    private String destination;

    public SmtpConnection(String destination) {
        super(MessageChannels.EMAIL);
        this.destination = destination;
    }
}
