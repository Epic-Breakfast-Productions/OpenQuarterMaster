package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.openlibrary;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "openlibrary")
public interface OpenLibraryLookupClient {
    @Path("/search.json")
    @WithSpan
    @GET
    @CacheResult(cacheName = "openlibrary")
    Uni<ObjectNode> search(
        @QueryParam("q") String query
    );
}
