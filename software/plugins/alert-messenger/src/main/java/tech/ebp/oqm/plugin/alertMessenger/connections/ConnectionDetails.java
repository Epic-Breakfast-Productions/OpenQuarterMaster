package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.utils.ConnectionType;

import java.time.Instant;

@Getter
@Setter
@MongoEntity(collection = "connectionDetails")
@BsonDiscriminator
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = EmailConnection.class, name = "EMAIL"),
    @JsonSubTypes.Type(value = DiscordConnection.class, name = "DISCORD"),
    @JsonSubTypes.Type(value = SlackConnection.class, name = "SLACK")
})
public abstract class ConnectionDetails extends PanacheMongoEntity {
    private String userId;
    private String lastError;
	private Instant lastErrorTime;
    private final ConnectionType type;
	private String name;
	private String description;

    ConnectionDetails(ConnectionType type) {
        this.type = type;
    }
}
