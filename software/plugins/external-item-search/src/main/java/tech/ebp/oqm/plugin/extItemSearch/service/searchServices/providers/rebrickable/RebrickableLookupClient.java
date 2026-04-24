package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.rebrickable;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v3/lego/")
@RegisterRestClient(configKey = "rebrickable")
public interface RebrickableLookupClient {
	
	@GET
	@Path("parts/{partNo}/")
	@WithSpan
	@CacheResult(cacheName = "rebrickable-part-num-get")
	Uni<ObjectNode> partFromNum(@HeaderParam("Authorization") String apiKey, @PathParam("partNo") String partNumber);
	
	@GET
	@Path("parts/")
	@WithSpan
	@CacheResult(cacheName = "rebrickable-part-search")
	Uni<ObjectNode> partsSearch(@HeaderParam("Authorization") String apiKey, @QueryParam("search") String query);
	
	@GET
	@Path("sets/{setNo}/")
	@WithSpan
	@CacheResult(cacheName = "rebrickable-set-num-get")
	Uni<ObjectNode> setFromNum(@HeaderParam("Authorization") String apiKey, @PathParam("setNo") String setNumber);
	
	@GET
	@Path("sets/")
	@WithSpan
	@CacheResult(cacheName = "rebrickable-set-search")
	Uni<ObjectNode> setsSearch(@HeaderParam("Authorization") String apiKey, @QueryParam("search") String query);
}
