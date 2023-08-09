package tech.ebp.oqm.baseStation.rest.restCalls.productLookup.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.concurrent.CompletionStage;

@Path("/api/items/")
@RegisterRestClient(configKey = "upc-datakick")
public interface DataKickLookupClient {
	@WithSpan
	@GET
	@Path("{upc}")
	CompletionStage<JsonNode> getFromUpcCode(@PathParam("upc") String barcode);
}
