package tech.ebp.oqm.core.api.interfaces.endpoints.info;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;

import java.util.Currency;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/info")
@Tags({@Tag(name = "Informational", description = "Endpoints for getting general information from the server.")})
@RequestScoped
public class GeneralInfo extends EndpointProvider {
	
	@ConfigProperty(name = "service.ops.currency")
	Currency currency;
	
	@GET
	@Path("currency")
	@Operation(
		summary = "The currency the api is set to operate with."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Currency getCurrency() {
		log.info("Getting currency of server.");
		return this.currency;
	}
	
}
