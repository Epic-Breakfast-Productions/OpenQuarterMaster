package tech.ebp.oqm.plugin.alertMessenger.connections.encryption;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;
import tech.ebp.oqm.plugin.alertMessenger.utils.ConnectionType;

@Getter
@Setter
public abstract class WebhookConnection extends ConnectionDetails {
	@NotBlank
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String webhookUrl;

	protected WebhookConnection(ConnectionType type) {
		super(type);
	}
}
