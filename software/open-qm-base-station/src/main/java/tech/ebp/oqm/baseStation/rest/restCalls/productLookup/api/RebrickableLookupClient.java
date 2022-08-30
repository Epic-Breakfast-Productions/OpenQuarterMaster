package tech.ebp.oqm.baseStation.rest.restCalls.productLookup.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.concurrent.CompletionStage;

@Traced
@Path("/api/v3/lego/")
@RegisterRestClient(configKey = "lego-rebrickable")
public interface RebrickableLookupClient {
	
	@Path("parts/{partNo}/")
	@GET
	CompletionStage<JsonNode> getFromPartNum(@QueryParam("key") String apiKey, @PathParam("partNo") String partNumber);
}
