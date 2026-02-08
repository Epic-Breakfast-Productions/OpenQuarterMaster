package tech.ebp.oqm.core.api.interfaces.endpoints.media.identifiers;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.service.identifiers.IdentifierBarcodeService;
import tech.ebp.oqm.core.api.service.identifiers.IdentifierUtils;

/**
 */
@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/media/code/identifier/")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@RolesAllowed(Roles.INVENTORY_VIEW)
@RequestScoped
public class IdentifierImageEndpoints extends EndpointProvider {
	
	@Inject
	IdentifierBarcodeService identifierBarcodeService;
	
	@GET
	@Path("{type}/{value}/{label}")
	@Operation(
		summary = "A barcode that represents the string given."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@Produces(IdentifierBarcodeService.DATA_MEDIA_TYPE)
	public Response getBarcode(
		@PathParam("type") IdentifierType type,
		@PathParam("value") String data,
		@PathParam("label") String label
	) {
		log.info("Getting {} barcode.", type);
		if(!IdentifierUtils.isValidCode(type, data)){
			return Response.status(Response.Status.BAD_REQUEST).entity("Value not a valid " + type).build();
		}
		return Response.status(Response.Status.OK)
				   .entity(this.identifierBarcodeService.getGeneralIdData(type, data, label))
				   .header("Content-Disposition", "attachment;filename="+label+"_"+data+".svg")
				   .type(IdentifierBarcodeService.DATA_MEDIA_TYPE)
				   .build();
	}
	
	@POST
	@Operation(
		summary = "A barcode that represents the general id given."
	)
	@APIResponse(
		responseCode = "200"
	)
	@Produces(IdentifierBarcodeService.DATA_MEDIA_TYPE)
	public Response getBarcode(
		Identifier identifier
	) {
		return this.getBarcode(
			identifier.getType(),
			identifier.getValue(),
			identifier.getLabel()
		);
	}
}
