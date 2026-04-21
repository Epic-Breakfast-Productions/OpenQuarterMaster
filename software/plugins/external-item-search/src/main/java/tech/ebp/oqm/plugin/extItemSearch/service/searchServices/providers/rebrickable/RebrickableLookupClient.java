package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.rebrickable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

@Path("/api/v3/lego/")
@RegisterRestClient(configKey = "rebrickable")
public interface RebrickableLookupClient {
	
	@GET
	@Path("parts/{partNo}/")
	@WithSpan
	Uni<ObjectNode> getFromPartNum(@QueryParam("key") String apiKey, @PathParam("partNo") String partNumber);
}
