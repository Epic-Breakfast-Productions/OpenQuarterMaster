package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.dataKick;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

@Path("/api/items/")
@RegisterRestClient(configKey = "datakick")
public interface DataKickLookupClient {
	
	@WithSpan
	@GET
	@Path("{upc}")
	@CacheResult(cacheName = "datakick-barcode-search")
	Uni<ArrayNode> getFromUpcCode(@PathParam("upc") String barcode);
}
