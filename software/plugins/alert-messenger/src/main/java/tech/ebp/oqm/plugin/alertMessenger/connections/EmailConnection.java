package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.utils.ConnectionType;

import java.util.Set;

@Getter
@Setter
@BsonDiscriminator(value = "EMAIL")
@JsonTypeName("EMAIL")
@MongoEntity(collection = "connectionDetails")
public class EmailConnection extends ConnectionDetails {

    private Set<@Email String> destinations;

    private Set<@Email String> cc;

    private Set<@Email String> bcc;

    public EmailConnection() {
        super(ConnectionType.EMAIL);
    }

    public EmailConnection(Set<String> destination) {
        this();
        this.destinations = destination;
    }
}
