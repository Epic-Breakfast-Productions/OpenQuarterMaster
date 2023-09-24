package tech.ebp.oqm.baseStation.rest.restCalls.productLookup.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
		UpcItemDbLookupClient.Request barcode
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
