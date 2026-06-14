package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.openfoodfacts;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "openfoodfacts")
public interface OpenFoodFactsLookupClient {
    //TODO: add filelds to limit the response size in service
    //https://openfoodfacts.github.io/documentation/docs/Product-Opener/v2/search/get-search/#limiting-results
    //TODO: add User-Agent header
    @WithSpan
    @GET
    @Path("/v3/product/{barcode}")
    @CacheResult(cacheName = "openfoodfacts-product")
    Uni<ObjectNode> getProduct(@PathParam("barcode") String barcode);

    @WithSpan
    @GET
    @Path("/v2/search")
    @CacheResult(cacheName = "openfoodfacts-search")
    Uni<ObjectNode> search(
        @QueryParam("categories_tags_en") String category,
        @QueryParam("page_size") int pageSize
    );
}
