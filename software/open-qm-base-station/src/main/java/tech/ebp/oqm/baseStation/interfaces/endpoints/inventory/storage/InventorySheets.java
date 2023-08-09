package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory.storage;

import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.rest.printouts.InventorySheetsOptions;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.printouts.StorageBlockInventorySheetService;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/inventory/storage-block/{id}/storageSheet")
@Tags({@Tag(name = "Storage Blocks")})
@RequestScoped
public class InventorySheets extends EndpointProvider {
	
	@jakarta.ws.rs.PathParam("id")
	private String id;
	
	@Inject
	StorageBlockInventorySheetService storageSheetService;
	
	@Blocking
	@GET
	@Operation(
		summary = "Creates a bundle of all inventory data stored."
	)
	@APIResponse(
		responseCode = "200",
		description = "Export bundle created.",
		content = @Content(
			mediaType = "application/pdf"
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces("application/pdf")
	public Response getSheetPdf(
		@Context SecurityContext securityContext,
		@BeanParam InventorySheetsOptions options
	) throws Throwable {
		Response.ResponseBuilder response = Response.ok(
			this.storageSheetService.getPdfInventorySheet(
				this.getInteractingEntity(),
				new ObjectId(this.id),
				options
			)
		);
		response.header("Content-Disposition", "attachment;filename=storageSheet-" + this.id + ".pdf");
		return response.build();
	}
}
