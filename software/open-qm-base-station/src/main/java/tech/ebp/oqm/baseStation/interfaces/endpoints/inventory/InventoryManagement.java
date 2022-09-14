package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory;


import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.rest.dataImportExport.DataImportResult;
import tech.ebp.oqm.baseStation.rest.dataImportExport.ImportBundleFileBody;
import tech.ebp.oqm.baseStation.service.importExport.DataExportService;
import tech.ebp.oqm.baseStation.service.importExport.DataImportService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.baseStation.service.scheduled.ExpiryProcessor;
import tech.ebp.oqm.baseStation.utils.UserRoles;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.File;
import java.io.IOException;

/**
 * https://mkyong.com/java/how-to-create-tar-gz-in-java/
 */
@Traced
@Slf4j
@Path("/api/inventory/manage")
@Tags({@Tag(name = "Inventory Management", description = "Endpoints for inventory management.")})
@RequestScoped
public class InventoryManagement extends EndpointProvider {
	
	
	@Inject
	JsonWebToken jwt;
	
	@Inject
	DataExportService dataExportService;
	
	@Inject
	DataImportService dataImportService;
	
	@Inject
	UserService userService;
	
	@Inject
	ExpiryProcessor expiryProcessor;
	
	@GET
	@Path("export")
	@Operation(
		summary = "Creates a bundle of all inventory data stored."
	)
	@APIResponse(
		responseCode = "200",
		description = "Export bundle created.",
		content = @Content(
			mediaType = "application/tar+gzip"
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(UserRoles.INVENTORY_ADMIN)
	@Produces("application/tar+gzip")
	public Response export(
		@Context SecurityContext securityContext,
		@QueryParam("excludeHistory") boolean excludeHistory
	) throws IOException {
		logRequestContext(this.jwt, securityContext);
		
		File outputFile = dataExportService.exportDataToBundle(excludeHistory);
		
		Response.ResponseBuilder response = Response.ok(outputFile);
		response.header("Content-Disposition", "attachment;filename=" + outputFile.getName());
		return response.build();
	}
	
	
	@POST
	@Path("import/file/bundle")
	@Operation(
		summary = "."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(UserRoles.INVENTORY_ADMIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response importData(
		@Context SecurityContext securityContext,
		@MultipartForm ImportBundleFileBody body
	) throws IOException {
		logRequestContext(this.jwt, securityContext);
		
		DataImportResult result = this.dataImportService.importBundle(body, this.userService.getFromJwt(this.jwt));
		
		return Response.ok(result).build();
	}
	
	@GET
	@Path("processExpiry")
	@Operation(
		summary = "Manually triggers the process to search for expired items and processing thereof."
	)
	@APIResponse(
		responseCode = "200",
		description = "Process triggered."
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(UserRoles.INVENTORY_ADMIN)
	public Response triggerSearchAndProcessExpiring(
		@Context SecurityContext securityContext
	) {
		logRequestContext(this.jwt, securityContext);
		
		expiryProcessor.searchAndProcessExpiring();
		
		return Response.ok().build();
	}
	
	//TODO:: prune histories
	//TODO:: piecemeal import
}
