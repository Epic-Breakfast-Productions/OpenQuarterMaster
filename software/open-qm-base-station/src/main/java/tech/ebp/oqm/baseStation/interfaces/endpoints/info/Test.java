package tech.ebp.oqm.baseStation.interfaces.endpoints.info;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/test")
@Tags({@Tag(name = "Test")})
@RequestScoped
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
	public Response illegalArgException() {
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
	public Response illegalStateException() {
		throw new IllegalStateException("bad");
	}
	
	@NoArgsConstructor
	public static class TestClass {
		
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
		@Valid TestClass test
	) {
		return Response.ok().build();
	}
	
}
