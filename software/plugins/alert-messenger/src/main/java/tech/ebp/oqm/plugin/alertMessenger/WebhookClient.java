package tech.ebp.oqm.plugin.alertMessenger;

import io.quarkus.rest.client.reactive.Url;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.WebhookMessage;

@RegisterRestClient(configKey = "webhook-client")
public interface WebhookClient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void send(@Url String url, WebhookMessage message);
}
