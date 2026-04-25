package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.upcItemDb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

@Path("/prod/")
@RegisterRestClient(configKey = "upcitemdb")
public interface UpcItemDbLookupClient {
	
	@WithSpan
	@POST
	@Path("v1/lookup")
	@Consumes(MediaType.APPLICATION_JSON)
	@CacheResult(cacheName = "upcitemdb-barcode-search")
	Uni<ObjectNode> getFromUpcCode(
		@HeaderParam("user_key") String key,
		@HeaderParam("key_type") String keyType,
		Request barcode
	);
	
	@WithSpan
	@GET
	@Path("trial/lookup")
	@Consumes(MediaType.APPLICATION_JSON)
	@CacheResult(cacheName = "upcitemdb-trial-barcode-search")
	Uni<ObjectNode> getFromUpcCodeTrial(
		@QueryParam("upc") String barcode
	);
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public class Request {
		@NonNull
		@NotNull
		private String upc;
	}
}
