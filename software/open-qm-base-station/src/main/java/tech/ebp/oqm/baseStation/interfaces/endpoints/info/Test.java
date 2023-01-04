package tech.ebp.oqm.baseStation.interfaces.endpoints.info;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Traced
@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/test")
@Tags({@Tag(name = "Test")})
@ApplicationScoped
public class Test extends EndpointProvider {
	
	@GET
	@Path("illegalArgException")
	@Operation(
		summary = "The currency the base station is set to operate with."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response illegalArgException(@Context SecurityContext ctx) {
		throw new IllegalArgumentException("bad");
	}
	
	@GET
	@Path("illegalStateException")
	@Operation(
		summary = "The currency the base station is set to operate with."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response illegalStateException(@Context SecurityContext ctx) {
		throw new IllegalStateException("bad");
	}
	
	@NoArgsConstructor
	public static class TestClass{
		@NotBlank
		public String field;
	}
	
	@POST
	@Path("validationException")
	@Operation(
		summary = "The currency the base station is set to operate with."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response validationException(
		@Context SecurityContext ctx,
		@Valid TestClass test
		) {
		return Response.ok().build();
	}
	
}
