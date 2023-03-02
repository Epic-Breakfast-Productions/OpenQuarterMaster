package tech.ebp.oqm.baseStation.interfaces.endpoints.auth;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jboss.resteasy.annotations.cache.NoCache;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.utils.TimeUtils;
import tech.ebp.oqm.lib.core.rest.auth.TokenCheckResponse;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Date;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/auth")
@Tags({@Tag(name = "Auth", description = "Endpoints for user authorization.")})
@RequestScoped
@NoCache
public class GeneralAuth extends EndpointProvider {
	@Inject
	JsonWebToken jwt;
	@Inject
	SecurityIdentity identity;
	
	@WithSpan
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
	public Response tokenCheck(@Context SecurityContext ctx) {
		logRequestContext(this.jwt, ctx);
		log.info("Checking user's token.");
		
		TokenCheckResponse response = new TokenCheckResponse();
		if (jwt.getRawToken() != null) {
			log.info("User roles: {}", this.identity.getRoles());
			log.info("User JWT claims: {}", this.jwt.getClaimNames());
			
			response.setHadToken(true);
			response.setTokenSecure(ctx.isSecure());
			response.setExpired(jwt.getExpirationTime() <= TimeUtils.currentTimeInSecs());
			response.setExpirationDate(new Date(jwt.getExpirationTime()));
		} else {
			log.info("User had no jwt");
		}
		return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(response).build();
	}
}
