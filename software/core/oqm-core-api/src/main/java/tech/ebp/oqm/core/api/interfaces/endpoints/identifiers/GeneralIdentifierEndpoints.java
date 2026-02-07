package tech.ebp.oqm.core.api.interfaces.endpoints.identifiers;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;
import tech.ebp.oqm.core.api.service.identifiers.general.GeneralIdUtils;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/identifiers/general")
@Tags({@Tag(name = "General Identifier", description = "Endpoints for getting/dealing with general identifiers.")})
@RequestScoped
public class GeneralIdentifierEndpoints extends EndpointProvider {
	
	@GET
	@Path("validate/{type}/{identifier}")
	@Operation(
		summary = "Validates that the given code is a valid one."
	)
	@APIResponse(
		responseCode = "200",
		description = "Identifier was valid for the given type."
	)
	@APIResponse(
		responseCode = "400",
		description = "Identifier was invalid for the given type."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateCode(
		@PathParam("type") IdentifierType type,
		@PathParam("identifier") String code
	) {
		try{
			return Response.ok(
				GeneralIdUtils.objFromParts(type, code)
			).build();
		} catch(IllegalArgumentException e){
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@GET
	@Path("getIdObject/{identifier}")
	@Operation(
		summary = "Gets the identifier object derived from the identifier string."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the code object."
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Identifier getIdObject(
		@PathParam("identifier") String code
	) {
		return GeneralIdUtils.determineGeneralIdType(code);
	}
	
}
