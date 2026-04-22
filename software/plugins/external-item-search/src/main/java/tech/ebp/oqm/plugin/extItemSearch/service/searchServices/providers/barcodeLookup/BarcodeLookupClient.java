package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.barcodeLookup;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/v3/products")
@RegisterRestClient(configKey = "barcodelookup-com")
public interface BarcodeLookupClient {
	
	@WithSpan
	@GET
	@CacheResult(cacheName = "barcodelookup-barcode-search")
	Uni<ObjectNode> searchBarcode(@QueryParam("key") String apiKey, @QueryParam("barcode") String barcode);
}
