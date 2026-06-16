package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.openfoodfacts;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "openfoodfacts")
@RegisterClientHeaders(OpenFoodFactsUserAgentHeadersFactory.class)
public interface OpenFoodFactsLookupClient {
    @WithSpan
    @GET
    @Path("/api/v3/product/{barcode}")
    @CacheResult(cacheName = "openfoodfacts-product")
    Uni<ObjectNode> getProduct(@PathParam("barcode") String barcode);
}
