package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.isbndb;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "isbndb")
public interface ISBNdbLookupClient {
    @WithSpan
    @GET
    @Path("/book/{isbn}")
    @CacheResult(cacheName = "isbndb-barcode")
    Uni<ObjectNode> searchBarcode(@HeaderParam("Authorization") String apiKey, @PathParam("isbn") String barcode);
}
