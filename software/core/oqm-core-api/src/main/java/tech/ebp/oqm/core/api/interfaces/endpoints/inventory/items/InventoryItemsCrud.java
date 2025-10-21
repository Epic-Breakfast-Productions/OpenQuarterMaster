package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.core.api.model.collectionStats.InvItemCollectionStats;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.InventoryItemSearch;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1_DB_AWARE + "/inventory/item")
@Tags({@Tag(name = "Inventory Items", description = "Endpoints for inventory item CRUD, and managing stored items.")})
@RequestScoped
public class InventoryItemsCrud extends MainObjectProvider<InventoryItem, InventoryItemSearch> {
	
	@Getter
	@Inject
	InventoryItemService objectService;
	
	@Getter
	Class<InventoryItem> objectClass = InventoryItem.class;
	
	@POST
	@Operation(
		summary = "Adds a new inventory item."
	)
	@APIResponse(
		responseCode = "200",
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
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public InventoryItem create(
		@Valid InventoryItem item
	) {
		return super.create(item);
	}
	
	@Override
	@Path("stats")
	@GET
	@Operation(
		summary = "Gets stats on this object's collection."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InvItemCollectionStats.class
			)
		)
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public InvItemCollectionStats getCollectionStats(
	) {
		return (InvItemCollectionStats) super.getCollectionStats();
	}
	
	@GET
	@Operation(
		summary = "Gets a list of items, using search parameters."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					type = SchemaType.OBJECT,
					implementation = SearchResult.class
				)
			)
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<InventoryItem> search(
		@BeanParam InventoryItemSearch itemSearch
	) {
		return super.search(itemSearch);
	}
	
	@Path("{itemId}")
	@GET
	@Operation(
		summary = "Gets a particular InventoryItem."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public InventoryItem get(
		@PathParam("itemId") String id
	) {
		return super.get(id);
	}
	
	@PUT
	@Path("{itemId}")
	@Operation(
		summary = "Updates a particular inventory item.",
		description = "Partial update to an item. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Produces(MediaType.APPLICATION_JSON)
	public InventoryItem update(
		@PathParam("itemId") String id,
		@Schema(
			implementation = InventoryItem.class
		)
		ObjectNode updates
	) {
		return super.update(id, updates);
	}
	
	@DELETE
	@Path("{itemId}")
	@Operation(
		summary = "Deletes a particular item."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object deleted.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has already been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "No object found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Produces(MediaType.APPLICATION_JSON)
	public InventoryItem delete(
		@PathParam("itemId") String id
	) {
		return super.delete(id);
	}
	
	@GET
	@Path("{itemId}/history")
	@Operation(
		summary = "Gets a particular object's history. Does not include history of stored items."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(type = SchemaType.ARRAY, implementation = ObjectHistoryEvent.class)
			)
		}
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "No history found for object with that id.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response getHistoryForObject(
		@PathParam("itemId") String id,
		@BeanParam HistorySearch searchObject
	) {
		return super.getHistoryForObject(id, searchObject);
	}
	
	@GET
	@Path("history")
	@Operation(
		summary = "Searches the history for the inventory items."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					type = SchemaType.ARRAY,
					implementation = ObjectHistoryEvent.class
				)
			)
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		return super.searchHistory(searchObject);
	}
	
}
