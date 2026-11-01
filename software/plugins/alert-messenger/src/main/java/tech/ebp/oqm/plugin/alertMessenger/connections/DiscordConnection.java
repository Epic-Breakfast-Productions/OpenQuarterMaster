package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.utils.ConnectionType;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.EncryptionService;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.EncryptedCredentials;

@Getter
@Setter
@BsonDiscriminator(value = "DISCORD")
@JsonTypeName("DISCORD")
@MongoEntity(collection = "connectionDetails")
public class DiscordConnection extends ConnectionDetails implements EncryptedCredentials {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String webhookUrl;

    public DiscordConnection() {
        super(ConnectionType.DISCORD);
    }

    public DiscordConnection(String webhookUrl) {
        this();
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void encryptCredentials(EncryptionService encryptionService) {
        if (webhookUrl != null && !webhookUrl.isBlank()) {
            webhookUrl = encryptionService.encrypt(webhookUrl);
        }
    }

    @Override
    public void decryptCredentials(EncryptionService encryptionService) {
        if (webhookUrl != null && !webhookUrl.isBlank()) {
            webhookUrl = encryptionService.decrypt(webhookUrl);
        }
    }
}
