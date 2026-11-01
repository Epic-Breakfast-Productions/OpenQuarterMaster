package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannel;

import java.util.Set;

@Getter
@Setter
@BsonDiscriminator(value = "EMAIL")
@JsonTypeName("EMAIL")
@MongoEntity(collection = "connectionDetails")
public class EmailConnection extends ConnectionDetails {

    private String destination;

    private Set<String> cc;

    private Set<String> bcc;

    public EmailConnection() {
        super(MessageChannel.EMAIL);
    }

    public EmailConnection(String destination) {
        this();
        this.destination = destination;
    }
}
