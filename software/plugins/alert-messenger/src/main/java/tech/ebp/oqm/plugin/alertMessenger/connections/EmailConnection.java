package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannels;

import java.util.Set;

@Getter
@Setter
@BsonDiscriminator(value = "EMAIL")
@JsonTypeName("EMAIL")
public class EmailConnection extends ConnectionDetails {

    private String destination;

    private Set<String> cc;

    private Set<String> bcc;

    public EmailConnection() {
        super(MessageChannels.EMAIL);
    }

    public EmailConnection(String destination) {
        this();
        this.destination = destination;
    }
}
