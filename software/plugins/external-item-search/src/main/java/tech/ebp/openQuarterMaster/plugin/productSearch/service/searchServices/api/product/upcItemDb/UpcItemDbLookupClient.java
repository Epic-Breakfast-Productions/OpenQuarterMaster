package tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.api.product.upcItemDb;

import com.fasterxml.jackson.databind.JsonNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.concurrent.CompletionStage;

@Path("/prod/")
@RegisterRestClient(configKey = "upc-upcitemdb")
public interface UpcItemDbLookupClient {
	
	@WithSpan
	@POST
	@Path("v1/lookup")
	@Consumes(MediaType.APPLICATION_JSON)
	CompletionStage<JsonNode> getFromUpcCode(
		@HeaderParam("user_key") String key,
		@HeaderParam("key_type") String keyType,
		Request barcode
	);
	
	@WithSpan
	@GET
	@Path("trial/lookup")
	@Consumes(MediaType.APPLICATION_JSON)
	CompletionStage<JsonNode> getFromUpcCodeTrial(
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
