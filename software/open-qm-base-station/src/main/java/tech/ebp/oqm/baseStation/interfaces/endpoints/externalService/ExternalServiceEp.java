package tech.ebp.oqm.baseStation.interfaces.endpoints.externalService;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.config.ExtServicesConfig;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.service.JwtService;
import tech.ebp.oqm.baseStation.service.PasswordService;
import tech.ebp.oqm.baseStation.service.mongo.ExternalServiceService;
import tech.ebp.oqm.baseStation.utils.AuthMode;
import tech.ebp.oqm.lib.core.object.history.events.externalService.ExtServiceSetupEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.externalService.ExternalService;
import tech.ebp.oqm.lib.core.rest.ErrorMessage;
import tech.ebp.oqm.lib.core.rest.auth.externalService.ExternalServiceLoginRequest;
import tech.ebp.oqm.lib.core.rest.auth.roles.Roles;
import tech.ebp.oqm.lib.core.rest.auth.user.UserLoginResponse;
import tech.ebp.oqm.lib.core.rest.externalService.ExternalServiceSetupRequest;
import tech.ebp.oqm.lib.core.rest.externalService.ExternalServiceSetupResponse;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/externalService")
@Tags({@Tag(name = "External Service", description = "Endpoints for external services to manage their interactions with this server.")})
@RequestScoped
public class ExternalServiceEp extends EndpointProvider {
	
	
	@ConfigProperty(name = "service.authMode")
	AuthMode authMode;
	
	@Inject
	ExtServicesConfig extServicesConfig;
	
	@Inject
	ExternalServiceService externalServiceService;
	
	@Inject
	PasswordService passwordService;
	
	@Inject
	JwtService jwtService;
	
	@WithSpan
	@POST
	@Path("setup/self")
	@Operation(
		summary = "Authenticates a user"
	)
	@APIResponse(
		responseCode = "202",
		description = "User was logged in.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = UserLoginResponse.class)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Incorrect credentials given.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "403",
		description = "If the account has been locked.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "429",
		description = "Happens when too many requests to login were sent in a given time period.",
		content = @Content(mediaType = "text/plain")
	)
	@PermitAll
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setupSelfAuthMode(
		@Context SecurityContext securityContext,
		@Valid ExternalServiceSetupRequest setupRequest
	) {
		assertSelfAuthMode(this.authMode);
		
		ExtServicesConfig.ExtServiceConfig serviceConfig = this.extServicesConfig.extServices().get(setupRequest.getName());
		
		if(serviceConfig == null){
			return Response.status(Response.Status.BAD_REQUEST)
					   .entity(new ErrorMessage("Service not found in available set."))
					   .build();
		}
		if(!serviceConfig.secret().equals(setupRequest.getSecret())){
			return Response.status(Response.Status.BAD_REQUEST)
						   .entity(new ErrorMessage("Bad client secret."))
						   .build();
		}
		
		ExternalService existentExtService = this.externalServiceService.getFromSetupRequest(setupRequest);
		
		String newToken = this.passwordService.getRandString(this.extServicesConfig.secretSizeMin(), this.extServicesConfig.secretSizeMax());
		
		existentExtService.setSetupTokenHash(this.passwordService.createPasswordHash(newToken));
		this.externalServiceService.update(existentExtService);
		this.externalServiceService.addHistoryFor(
			existentExtService,
			null,
			new ExtServiceSetupEvent(existentExtService, null)
		);
		
		ExternalServiceSetupResponse.Builder<?, ?> builder = ExternalServiceSetupResponse.builder();
		
		builder.setupToken(newToken);
		builder.id(existentExtService.getId());
		
		return Response.ok(builder.build()).build();
	}
	
	@WithSpan
	@POST
	@Path("setup/external")
	@Operation(
		summary = "Sets up an external service. Only call when AuthMode is EXTERNAL"
	)
	@APIResponse(
		responseCode = "202",
		description = "User was logged in.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ExternalServiceSetupRequest.class)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Incorrect credentials given.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "403",
		description = "If the account has been locked.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "429",
		description = "Happens when too many requests to login were sent in a given time period.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.EXT_SERVICE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setupExternalAuthMode(
		@Context SecurityContext securityContext,
		@Valid ExternalServiceSetupRequest setupRequest
	) {
		assertExternalAuthMode(this.authMode);
		//TODO:: BS-37
		return Response.ok().build();
	}
	
	@WithSpan
	@POST
	@Path("auth")
	@Operation(
		summary = "Authenticates an external service"
	)
	@APIResponse(
		responseCode = "202",
		description = "User was logged in.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ExternalServiceLoginRequest.class)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Incorrect credentials given.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "403",
		description = "If the account has been locked.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "429",
		description = "Happens when too many requests to login were sent in a given time period.",
		content = @Content(mediaType = "text/plain")
	)
	@PermitAll
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response auth(
		@Context SecurityContext securityContext,
		@Valid ExternalServiceLoginRequest loginRequest
	) {
		assertSelfAuthMode(this.authMode);
		
		ExternalService service = this.externalServiceService.get(loginRequest.getId());
		
		if(!this.passwordService.passwordMatchesHash(service, loginRequest)){
			log.warn("Service gave invalid token.");
			return Response.status(Response.Status.BAD_REQUEST).entity("Bad token.").build();
		}
		
		return Response.ok().entity(
			this.jwtService.getExtServiceJwt(service)
		).build();
	}
}
