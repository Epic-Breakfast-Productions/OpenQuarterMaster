package tech.ebp.oqm.core.api.interfaces.endpoints.inventory;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.ItemCheckoutSearch;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1_DB_AWARE + "/inventory/item-checkout")
@Tags({@Tag(name = "Item Checkout", description = "Endpoints for managing Item Checkouts.")})
@RequestScoped
public class ItemCheckoutCrud extends MainObjectProvider<ItemCheckout, ItemCheckoutSearch> {
	
	@Inject
	@Getter
	ItemCheckoutService objectService;
	
	@Getter
	Class<ItemCheckout> objectClass =  ItemCheckout.class;

	@GET
	@Operation(
		summary = "Gets a list of checkouts, using search parameters."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved."
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Override
	public SearchResult<ItemCheckout> search(
		@BeanParam ItemCheckoutSearch itemCheckoutSearch
	) {
		return super.search(itemCheckoutSearch);
	}
	
	@Path("{id}")
	@GET
	@Operation(
		summary = "Gets a particular Item Checkout."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ItemCheckout.class
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
	@Override
	public ItemCheckout get(
		@PathParam("id") String id
	) {
		return super.get(id);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a checkout.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Storage block updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ItemCheckout.class
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
	@Override
	public ItemCheckout update(
		@PathParam("id") String id,
		ObjectNode updates
	) {
		return super.update(id, updates);
	}
	
	@DELETE
	@Path("{id}")
	@Operation(
		summary = "Deletes a particular ItemCheckout."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object deleted.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ItemCheckout.class
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
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ItemCheckout delete(
		@PathParam("id") String id
	) {
		return super.delete(id);
	}
	
	//<editor-fold desc="History">
	@GET
	@Path("{id}/history")
	@Operation(
		summary = "Gets a particular object's history."
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
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject
	) {
		return super.getHistoryForObject(id, searchObject);
	}
	
	@GET
	@Path("history")
	@Operation(
		summary = "Searches the history for the categories."
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
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		return super.searchHistory(searchObject);
	}
	
	//</editor-fold>
}
