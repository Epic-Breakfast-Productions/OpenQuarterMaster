package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.inventory.items;

import com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.MainObjectProvider;
import com.ebp.openQuarterMaster.baseStation.rest.search.InventoryItemSearch;
import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.lib.core.storage.items.InventoryItem;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Traced
@Slf4j
@Path("/api/inventory/item")
@Tags({@Tag(name = "Inventory Items", description = "Endpoints for inventory item CRUD, and managing stored items.")})
@RequestScoped
public class InventoryItemsCrud extends MainObjectProvider<InventoryItem, InventoryItemSearch> {
	
	@Inject
	public InventoryItemsCrud(
		InventoryItemService inventoryItemService,
		UserService userService,
		JsonWebToken jwt
	) {
		super(inventoryItemService, userService, jwt);
	}
	
	@POST
	@Operation(
		summary = "Adds a new inventory item."
	)
	@APIResponse(
		responseCode = "201",
		description = "Item added.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ObjectId.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.)",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed("user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String create(
		@Context SecurityContext securityContext,
		@Valid InventoryItem item
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Creating new item.");
		
		ObjectId output = this.getObjectService().add(item, this.getUserFromJwt());
		log.info("Item created with id: {}", output);
		return output.toHexString();
	}
	
	
	@GET
	@Operation(
		summary = "Gets a list of inventory items."
	)
	@APIResponse(
		responseCode = "200",
		description = "Items retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				type = SchemaType.ARRAY,
				implementation = InventoryItem.class
			)
		),
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@APIResponse(
		responseCode = "204",
		description = "No items found from query given.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed("user")
	@Produces({MediaType.APPLICATION_JSON})
	public Response search(
		@Context SecurityContext securityContext,
		//for actual queries
		@BeanParam InventoryItemSearch search
	) {
		logRequestContext(this.getJwt(), securityContext);
		
		SearchResult<InventoryItem> searchResult = this.getObjectService().search(search);
		
		return Response
			.status(Response.Status.OK)
			.entity(searchResult.getResults())
			.header("num-elements", searchResult.getResults().size())
			.header("query-num-results", searchResult.getNumResultsForEntireQuery())
			.build();
	}
	
	@GET
	@Path("{itemId}/{storageBlockId}")
	@Operation(
		summary = "Gets the stored amount or tracked item to the storage block specified."
	)
	@APIResponse(
		responseCode = "200",
		description = "Item added.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "No item found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed("user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStoredInventoryItem(
		@Context SecurityContext securityContext,
		@PathParam String itemId,
		@PathParam String storageBlockId
	) {
		logRequestContext(this.getJwt(), securityContext);
		//TODO
		return Response.serverError().entity("Not implemented yet.").build();
	}
	
	@PUT
	@Path("{itemId}/{storageBlockId}")
	@Operation(
		summary = "Adds a stored amount or tracked item to the storage block specified."
	)
	@APIResponse(
		responseCode = "200",
		description = "Item added.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "No item found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("user")
	public Response addStoredInventoryItem(
		@Context SecurityContext securityContext,
		@PathParam String itemId,
		@PathParam String storageBlockId
	) {
		logRequestContext(this.getJwt(), securityContext);
		//TODO
		return Response.serverError().entity("Not implemented yet.").build();
	}
	
	@DELETE
	@Path("{itemId}/{storageBlockId}")
	@Operation(
		summary = "Removes a stored amount or tracked item from the storage block specified."
	)
	@APIResponse(
		responseCode = "200",
		description = "Item added.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "No item found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("user")
	public Response removeStoredInventoryItem(
		@Context SecurityContext securityContext,
		@PathParam String itemId,
		@PathParam String storageBlockId
	) {
		logRequestContext(this.getJwt(), securityContext);
		//TODO
		return Response.serverError().entity("Not implemented yet.").build();
	}
}
