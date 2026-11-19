package tech.ebp.oqm.plugin.alertMessenger.utils.webhooks;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;

import java.io.Closeable;

public interface WebhookClient extends Closeable {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void send(WebhookMessage message);
}
