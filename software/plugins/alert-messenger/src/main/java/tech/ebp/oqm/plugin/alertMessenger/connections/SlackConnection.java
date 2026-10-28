package tech.ebp.oqm.plugin.alertMessenger.connections;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.EncryptedCredentials;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.EncryptionService;
import tech.ebp.oqm.plugin.alertMessenger.utils.MessageChannel;

@Getter
@Setter
@BsonDiscriminator(value = "SLACK")
@JsonTypeName("SLACK")
public class SlackConnection extends ConnectionDetails implements EncryptedCredentials {

    @Inject
    EncryptionService encryptionService;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String webhookUrl;

    private String name;

    public SlackConnection() {
        super(MessageChannel.SLACK);
    }

    public SlackConnection(String webhookUrl) {
        this();
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void encryptCredentials() {
        if(webhookUrl != null && !webhookUrl.isBlank()) {
            webhookUrl = encryptionService.encrypt(webhookUrl);
        }
    }

    @Override
    public void decryptCredentials() {
        if(webhookUrl != null && !webhookUrl.isBlank()) {
            webhookUrl = encryptionService.decrypt(webhookUrl);
        }
    }
}
