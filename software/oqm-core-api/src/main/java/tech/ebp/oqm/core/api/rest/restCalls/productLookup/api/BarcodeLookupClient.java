package tech.ebp.oqm.core.api.rest.restCalls.productLookup.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

@Path("/v3/products")
@RegisterRestClient(configKey = "upc-barcodelookup-com")
public interface BarcodeLookupClient {
	@WithSpan
	@GET
	CompletionStage<JsonNode> getFromUpcCode(@QueryParam("key") String apiKey, @QueryParam("barcode") String barcode);
}
