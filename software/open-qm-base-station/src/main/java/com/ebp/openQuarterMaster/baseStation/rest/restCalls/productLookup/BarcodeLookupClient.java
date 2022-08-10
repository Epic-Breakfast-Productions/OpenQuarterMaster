package com.ebp.openQuarterMaster.baseStation.rest.restCalls.productLookup;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.concurrent.CompletionStage;

@Traced
@Path("/v3/products")
@RegisterRestClient(configKey = "upc-barcodelookup-com")
public interface BarcodeLookupClient {
	@GET
	CompletionStage<JsonNode> getFromUpcCode(@QueryParam("key") String apiKey, @QueryParam("barcode") String barcode);
}
