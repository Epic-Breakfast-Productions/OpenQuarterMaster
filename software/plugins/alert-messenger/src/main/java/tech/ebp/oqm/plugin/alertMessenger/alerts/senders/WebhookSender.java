package tech.ebp.oqm.plugin.alertMessenger.alerts.senders;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.inject.Inject;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.EncryptionService;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.WebhookConnection;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.WebhookClient;

import java.net.URI;

public abstract class WebhookSender {
	@Inject
	EncryptionService encryptionService;

	protected WebhookClient buildClient(WebhookConnection connection) {
		try {
			String webhookUrl = encryptionService.decrypt(connection.getWebhookUrl());
			return QuarkusRestClientBuilder.newBuilder()
				.baseUri(URI.create(webhookUrl))
				.build(WebhookClient.class);
		} catch (RuntimeException e) {
			throw new IllegalStateException("Failed to build WebhookClient; check the webhook URL and encryption settings", e);
		}
	}

}
