package tech.ebp.oqm.baseStation.rest.restCalls.productLookup.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.concurrent.CompletionStage;

@Path("/v3/products")
@RegisterRestClient(configKey = "upc-barcodelookup-com")
public interface BarcodeLookupClient {
	@WithSpan
	@GET
	CompletionStage<JsonNode> getFromUpcCode(@QueryParam("key") String apiKey, @QueryParam("barcode") String barcode);
}
