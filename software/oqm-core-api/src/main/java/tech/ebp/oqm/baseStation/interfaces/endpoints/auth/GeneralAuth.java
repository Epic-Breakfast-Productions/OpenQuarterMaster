package tech.ebp.oqm.baseStation.interfaces.endpoints.auth;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jboss.resteasy.reactive.NoCache;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.model.rest.auth.TokenCheckResponse;
import tech.ebp.oqm.baseStation.utils.TimeUtils;

import java.util.Date;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/auth")
@Tags({@Tag(name = "Auth", description = "Endpoints for user authorization.")})
@RequestScoped
@NoCache
public class GeneralAuth extends EndpointProvider {
	@Inject
	SecurityIdentity identity;
	
	@GET
	@Path("tokenCheck")
	@Operation(
		summary = "Checks a users' token."
	)
	@APIResponse(
		responseCode = "200",
		description = "The check happened.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = TokenCheckResponse.class)
		)
	)
	@SecurityRequirement(name = "JwtAuth")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response tokenCheck() {
		log.info("Checking user's token.");
		
		TokenCheckResponse response = new TokenCheckResponse();
		if (this.getUserToken().getRawToken() != null) {
			log.info("User roles: {}", this.identity.getRoles());
			log.info("User JWT claims: {}", this.getUserToken().getClaimNames());
			
			response.setHadToken(true);
			response.setTokenSecure(this.getSecurityContext().isSecure());
			response.setExpired(this.getUserToken().getExpirationTime() <= TimeUtils.currentTimeInSecs());
			response.setExpirationDate(new Date(this.getUserToken().getExpirationTime()));
		} else {
			log.info("User had no jwt");
		}
		return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(response).build();
	}
}
