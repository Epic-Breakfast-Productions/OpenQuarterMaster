package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.WebhookConnection;
import tech.ebp.oqm.plugin.alertMessenger.utils.ConnectionType;

@Getter
@Setter
@BsonDiscriminator(value = "DISCORD")
@JsonTypeName("DISCORD")
@MongoEntity(collection = "connectionDetails")
public class DiscordConnection extends WebhookConnection {

    public DiscordConnection() {
        super(ConnectionType.DISCORD);
    }

	public DiscordConnection(String webhookUrl) {
		this();
		setWebhookUrl(webhookUrl);
	}
}
