package tech.ebp.oqm.plugin.alertMessenger.alerts.senders;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.inject.Inject;
import tech.ebp.oqm.plugin.alertMessenger.WebhookClient;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.EncryptionService;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.WebhookConnection;

import java.net.URI;

public abstract class WebhookSender {
    @Inject
    EncryptionService encryptionService;

    protected WebhookClient buildClient(WebhookConnection connection) {
        String webhookUrl = encryptionService.decrypt(connection.getWebhookUrl());
        return QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(webhookUrl))
            .build(WebhookClient.class);
    }
}
