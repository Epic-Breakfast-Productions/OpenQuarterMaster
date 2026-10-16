package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

@Getter
@Setter
@BsonDiscriminator(value = "EMAIL")
@JsonTypeName("EMAIL")
public class SmtpConnection extends ConnectionDetails {

    private String destination;

    public SmtpConnection() {
        super(MessageChannels.EMAIL);
    }

    public SmtpConnection(String destination) {
        this();
        this.destination = destination;
    }
}
