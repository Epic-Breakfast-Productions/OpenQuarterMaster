package tech.ebp.oqm.core.baseStation.interfaces.rest;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.baseStation.model.printouts.InventorySheetsOptions;
import tech.ebp.oqm.core.baseStation.service.printout.StorageBlockInventorySheetService;
import tech.ebp.oqm.core.baseStation.utils.Roles;

@Slf4j
@Path("/api/media/printouts")
@Tags({@Tag(name = "Media", description = "Endpoints for printouts")})
@RolesAllowed(Roles.INVENTORY_VIEW)
@RequestScoped
public class Printouts extends ApiProvider {

	@Inject
	StorageBlockInventorySheetService storageBlockInventorySheetService;

	@GET
	@Path("storage-block/{storageBlockId}/storageSheet")
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
	@Blocking
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces("application/pdf")
	public Response getSheetPdf(
		@PathParam("storageBlockId") String storageBlockId,
		@BeanParam InventorySheetsOptions options
	) throws Throwable {
		Response.ResponseBuilder response = Response.ok(
			this.storageBlockInventorySheetService.getPdfInventorySheet(
				this.getUserInfo(),
				this.getBearerHeaderStr(),
				this.getSelectedDb(),
				storageBlockId,
				options
			)
		);
		response.header("Content-Disposition", "attachment;filename=storageSheet-" + storageBlockId + ".pdf");
		return response.build();
	}
}
