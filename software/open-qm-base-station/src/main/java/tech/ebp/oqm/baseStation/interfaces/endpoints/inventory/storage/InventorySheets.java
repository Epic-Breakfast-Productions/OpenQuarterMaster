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
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.rest.printouts.InventorySheetsOptions;
import tech.ebp.oqm.baseStation.service.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.printouts.StorageBlockInventorySheetService;
import tech.ebp.oqm.lib.core.rest.auth.roles.Roles;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;

@Traced
@Slf4j
@Path("/api/inventory/storage-block/{id}/storageSheet")
@Tags({@Tag(name = "Storage Blocks")})
@RequestScoped
public class InventorySheets extends EndpointProvider {
	
	@javax.ws.rs.PathParam("id")
	private String id;
	
	@Inject
	JsonWebToken jwt;
	
	@Inject
	StorageBlockInventorySheetService storageSheetService;
	
	@Inject
	InteractingEntityService interactingEntityService;
	
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
	) throws IOException {
		logRequestContext(this.jwt, securityContext);
		
		Response.ResponseBuilder response = Response.ok(
			this.storageSheetService.getPdfInventorySheet(
				this.interactingEntityService.getFromJwt(this.jwt),
				new ObjectId(this.id),
				options
			)
		);
		response.header("Content-Disposition", "attachment;filename=storageSheet-" + this.id + ".pdf");
		return response.build();
	}
}
