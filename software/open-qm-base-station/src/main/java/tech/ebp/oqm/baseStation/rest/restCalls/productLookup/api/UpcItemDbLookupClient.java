package tech.ebp.oqm.baseStation.rest.restCalls.productLookup.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;

@Traced
@Path("/prod/")
@RegisterRestClient(configKey = "upc-upcitemdb")
public interface UpcItemDbLookupClient {
	@Traced
	@POST
	@Path("v1/lookup")
	@Consumes(MediaType.APPLICATION_JSON)
	CompletionStage<JsonNode> getFromUpcCode(
		@HeaderParam("user_key") String key,
		@HeaderParam("key_type") String keyType,
		UpcItemDbLookupClient.Request barcode
	);
	
	@Traced
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
