package tech.ebp.oqm.baseStation.interfaces.endpoints.info;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Variant;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.model.units.UnitCategory;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;

import javax.measure.Unit;
import java.util.Currency;
import java.util.Map;
import java.util.Set;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/info")
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
