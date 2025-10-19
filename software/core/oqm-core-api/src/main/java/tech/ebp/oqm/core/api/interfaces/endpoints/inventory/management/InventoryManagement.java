package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.management;


import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.dataImportExport.DataImportResult;
import tech.ebp.oqm.core.api.model.rest.dataImportExport.ImportBundleFileBody;
import tech.ebp.oqm.core.api.model.rest.management.DbClearResult;
import tech.ebp.oqm.core.api.scheduled.ExpiryProcessor;
import tech.ebp.oqm.core.api.service.importExport.exporting.DatabaseExportService;
import tech.ebp.oqm.core.api.service.importExport.importing.DataImportService;
import tech.ebp.oqm.core.api.service.importExport.exporting.DataExportOptions;
import tech.ebp.oqm.core.api.service.mongo.DatabaseManagementService;
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * TODO:: refactor to more specific classes
 *
 * https://mkyong.com/java/how-to-create-tar-gz-in-java/
 */
@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/inventory/manage")
@Tags({@Tag(name = "Inventory Management", description = "Endpoints for inventory management.")})
@RequestScoped
public class InventoryManagement extends EndpointProvider {
	
	@Inject
	DatabaseExportService databaseExportService;
	
	@Inject
	DataImportService dataImportService;
	
	@Inject
	ExpiryProcessor expiryProcessor;
	
	@Inject
	DatabaseManagementService dbms;

	@Inject
	OqmDatabaseService oqmDatabaseService;
	
	@Blocking
	@GET
	@Path("db/{oqmDbIdOrName}/export")
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
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Produces("application/tar+gzip")
	public Response export(
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName
			//TODO:: options as bean param? figure this out
	) throws IOException {
		File outputFile = databaseExportService.exportDataToBundle(new DataExportOptions());
		
		Response.ResponseBuilder response = Response.ok(outputFile);
		response.header("Content-Disposition", "attachment;filename=" + outputFile.getName());
		return response.build();
	}
	
	@Blocking
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
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response importData(
		@BeanParam ImportBundleFileBody body
	) throws IOException {
		DataImportResult result = this.dataImportService.importBundle(
			body.file,
			body.fileName,
			this.getInteractingEntity(),
			body.options
		);
		
		return Response.ok(result).build();
	}
	
	@Blocking
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
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	public Response triggerSearchAndProcessExpiring() {
		expiryProcessor.searchAndProcessExpiring();

		return Response.ok().build();
	}
	
	@Blocking
	@DELETE
	@Path("/db/clear/{oqmDbIdOrName}")
	@Operation(
		summary = "Manually triggers the process to clear the database."
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
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	public DbClearResult clearDatabase(
		@PathParam("oqmDbIdOrName")
		String oqmDbIdOrName
	) throws Exception {
		try (MongoSessionWrapper csw = new MongoSessionWrapper(null, this.getInteractingEntityService())) {
			return csw.runTransaction(() -> {
				return this.dbms.clearDb(csw.getClientSession(), oqmDbIdOrName, this.getInteractingEntity());
			});
		} catch (Exception e) {
			log.error("Failed to apply transaction: ", e);
			throw e;
		}
	}


	@Blocking
	@DELETE
	@Path("/db/clearAllDbs")
	@Operation(
		summary = "Manually triggers the process to clear the database."
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
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	public List<DbClearResult> clearAllDatabases(
	) throws Exception {
		return this.dbms.clearAllDbs(this.getInteractingEntity());
	}

	@Blocking
	@POST
	@Path("db")
	@Operation(
		summary = "Add a Database"
	)
	@APIResponse(
		responseCode = "200",
		description = "Database added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addDb(
		@Valid OqmMongoDatabase body
	) {
		log.info("Creating new database from REST call: {}", body);
		ObjectId result = this.oqmDatabaseService.addOqmDatabase(body);
		log.info("Created new database from REST call: {}", result);
		return Response.ok(result).build();
	}

	@Blocking
	@PUT
	@Path("db/ensure/{dbName}")
	@Operation(
		summary = "Ensures a Database with a particular name exists."
	)
	@APIResponse(
		responseCode = "200",
		description = "Database added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response ensureDb(
		@PathParam("dbName") String dbName
	) {
		log.info("Creating new database from REST call: {}", dbName);
		boolean result = this.oqmDatabaseService.ensureDatabase(dbName);
		log.info("Created new database from REST call: {}", result);
		return Response.ok(result).build();
	}

	@Blocking
	@GET
	@Path("db")
	@Operation(
		summary = "List Databases"
	)
	@APIResponse(
		responseCode = "200",
		description = "Database added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listDatabases() {
		return Response.ok(this.oqmDatabaseService.listIterator().into(new ArrayList<>())).build();
	}

	@Blocking
	@GET
	@Path("db/refreshCache")
	@Operation(
		summary = "Refreshes the internal database cache."
	)
	@APIResponse(
		responseCode = "200",
		description = "Database added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response refreshDbCache() {
		this.oqmDatabaseService.refreshCache();
		return Response.ok().build();
	}
	
	//TODO:: prune histories
	//TODO:: piecemeal import
}
