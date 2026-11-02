package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.WebhookConnection;
import tech.ebp.oqm.plugin.alertMessenger.utils.ConnectionType;

@Getter
@Setter
@BsonDiscriminator(value = "SLACK")
@JsonTypeName("SLACK")
@MongoEntity(collection = "connectionDetails")
public class SlackConnection extends WebhookConnection {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String webhookUrl;

    public SlackConnection() {
        super(ConnectionType.SLACK);
    }

    public SlackConnection(String webhookUrl) {
        this();
        this.webhookUrl = webhookUrl;
    }
}
