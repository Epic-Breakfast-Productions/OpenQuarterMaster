package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.deezer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "deezer")
public interface DeezerLookupClient {

	@Path("/search/album")
	@WithSpan
	@GET
	@CacheResult(cacheName = "deezer-search")
	Uni<ObjectNode> search(@QueryParam("q") String query);

	@Path("/album/{id}")
	@WithSpan
	@GET
	@CacheResult(cacheName = "deezer-album")
	Uni<ObjectNode> getAlbum(@PathParam("id") String id);
}
