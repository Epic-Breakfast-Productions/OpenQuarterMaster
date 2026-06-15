package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.openfoodfacts;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "openfoodfacts-search")
public interface OpenFoodFactsSearchClient {
    @WithSpan
    @GET
    @Path("/search")
    @CacheResult(cacheName = "openfoodfacts-search")
    Uni<ObjectNode> search(
        @QueryParam("page_size") int pageSize,
        @QueryParam("q") String query
    );
}
