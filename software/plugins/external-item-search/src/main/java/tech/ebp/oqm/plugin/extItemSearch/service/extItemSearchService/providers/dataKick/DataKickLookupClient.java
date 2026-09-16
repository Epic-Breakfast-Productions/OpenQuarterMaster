package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.dataKick;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/items/")
@RegisterRestClient(configKey = "datakick")
public interface DataKickLookupClient {
	
	@WithSpan
	@GET
	@Path("{upc}")
	@CacheResult(cacheName = "datakick-barcode-search")
	Uni<ArrayNode> getFromUpcCode(@PathParam("upc") String barcode);
}
