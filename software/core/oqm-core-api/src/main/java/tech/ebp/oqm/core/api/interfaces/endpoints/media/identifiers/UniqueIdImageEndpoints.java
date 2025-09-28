package tech.ebp.oqm.core.api.interfaces.endpoints.media.identifiers;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
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
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralIdType;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.service.identifiers.general.GeneralIdBarcodeService;
import tech.ebp.oqm.core.api.service.identifiers.unique.UniqueIdBarcodeService;

/**
 */
@Slf4j
@Path("/api/media/code/identifier/unique")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@RolesAllowed(Roles.INVENTORY_VIEW)
@RequestScoped
public class UniqueIdImageEndpoints extends EndpointProvider {
	
	@Inject
	UniqueIdBarcodeService uniqueIdBarcodeService;
	
	@GET
	@Path("{value}")
	@Operation(
		summary = "A barcode that represents the string given."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@Produces(GeneralIdBarcodeService.DATA_MEDIA_TYPE)
	public Response getBarcode(
		@PathParam("value") String data
	) {
		log.info("Getting barcode for unique id value: {}", data);
		
		return Response.status(Response.Status.OK)
				   .entity(this.uniqueIdBarcodeService.getUniqueIdData(data))
				   .header("Content-Disposition", "attachment;filename=" + "code.svg")
				   .type(UniqueIdBarcodeService.DATA_MEDIA_TYPE)
				   .build();
	}
	
}
