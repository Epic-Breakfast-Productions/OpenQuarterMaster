package tech.ebp.oqm.core.api.rest.restCalls.productLookup.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

@Path("/api/v3/lego/")
@RegisterRestClient(configKey = "lego-rebrickable")
public interface RebrickableLookupClient {
	
	@WithSpan
	@Path("parts/{partNo}/")
	@GET
	CompletionStage<JsonNode> getFromPartNum(@QueryParam("key") String apiKey, @PathParam("partNo") String partNumber);
}
