package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.NoArgsConstructor;
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
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.ItemCheckoutSearch;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails.CheckInDetails;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkoutFor.CheckoutForOqmEntity;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.storage.itemCheckout.ItemCheckoutRequest;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/inventory/item-checkout")
@Tags({@Tag(name = "Item Checkout", description = "Endpoints for managing Item Checkouts.")})
@RequestScoped
@NoArgsConstructor
public class ItemCheckoutCrud extends MainObjectProvider<ItemCheckout, ItemCheckoutSearch> {
	
	Template itemCheckoutSearchResultsTemplate;
	
	@Inject
	public ItemCheckoutCrud(
		ItemCheckoutService itemCheckoutService,
		InteractingEntityService interactingEntityService,
		JsonWebToken jwt,
		@Location("tags/objView/history/searchResults.html")
		Template historyRowsTemplate,
		@Location("tags/search/itemCheckout/searchResults.html")
		Template itemCheckoutSearchResultsTemplate
	) {
		super(ItemCheckout.class, itemCheckoutService, interactingEntityService, jwt, historyRowsTemplate);
		this.itemCheckoutSearchResultsTemplate = itemCheckoutSearchResultsTemplate;
	}
	
	@POST
	@Operation(
		summary = "Checks out an item."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
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
	@RolesAllowed({Roles.INVENTORY_EDIT, Roles.INVENTORY_CHECKOUT})//TODO:: add checkout role to test keycloak
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectId create(
		@Context SecurityContext securityContext,
		@Valid ItemCheckoutRequest itemCheckoutRequest
	) {
		logRequestContext(this.getJwt(), securityContext);
		
		InteractingEntity entity = this.getInteractingEntityFromJwt();
		
		if(itemCheckoutRequest.getCheckedOutFor() == null){
			itemCheckoutRequest.setCheckedOutFor(
				new CheckoutForOqmEntity(entity.getReference())
			);
		}
		
		return ((ItemCheckoutService)this.getObjectService()).checkoutItem(itemCheckoutRequest, entity);
	}
	
	@PUT
	@Path("{id}/checkin")
	@Operation(
		summary = "Checks in an item."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
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
	@RolesAllowed({Roles.INVENTORY_EDIT, Roles.INVENTORY_CHECKOUT})//TODO:: add checkout role to test keycloak
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ItemCheckout checkin(
		@Context SecurityContext securityContext,
		@PathParam("id") String id,
		@Valid CheckInDetails checkInDetails
	) {
		logRequestContext(this.getJwt(), securityContext);
		InteractingEntity entity = this.getInteractingEntityFromJwt();
		
		return ((ItemCheckoutService)this.getObjectService()).checkinItem(new ObjectId(id), checkInDetails, entity);
	}
	
	@GET
	@Operation(
		summary = "Gets a list of storage blocks, using search parameters."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					type = SchemaType.ARRAY,
					implementation = ItemCheckout.class
				)
			),
			@Content(
				mediaType = "text/html",
				schema = @Schema(type = SchemaType.STRING)
			)
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Override
	public Response search(
		@Context SecurityContext securityContext,
		@BeanParam ItemCheckoutSearch itemCheckoutSearch
	) {
		Tuple2<Response.ResponseBuilder, SearchResult<ItemCheckout>> tuple = super.getSearchResponseBuilder(securityContext, itemCheckoutSearch);
		Response.ResponseBuilder rb = tuple.getItem1();
		
		log.debug("Accept header value: \"{}\"", itemCheckoutSearch.getAcceptHeaderVal());
		switch (itemCheckoutSearch.getAcceptHeaderVal()) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				SearchResult<ItemCheckout> output = tuple.getItem2();
				rb = rb.entity(
						   this.itemCheckoutSearchResultsTemplate
							   .data("searchResults", output)
							   .data("actionType", (
								   itemCheckoutSearch.getActionTypeHeaderVal() == null || itemCheckoutSearch.getActionTypeHeaderVal().isBlank() ? "full" :
									   itemCheckoutSearch.getActionTypeHeaderVal()
							   ))
							   .data(
								   "searchFormId",
								   (
									   itemCheckoutSearch.getSearchFormIdHeaderVal() == null || itemCheckoutSearch.getSearchFormIdHeaderVal().isBlank() ?
										   "" :
										   itemCheckoutSearch.getSearchFormIdHeaderVal()
								   )
							   )
							   .data(
								   "inputIdPrepend",
								   (
									   itemCheckoutSearch.getInputIdPrependHeaderVal() == null || itemCheckoutSearch.getInputIdPrependHeaderVal().isBlank() ?
										   "" :
										   itemCheckoutSearch.getInputIdPrependHeaderVal()
								   )
							   )
							   .data(
								   "otherModalId",
								   (
									   itemCheckoutSearch.getOtherModalIdHeaderVal() == null || itemCheckoutSearch.getOtherModalIdHeaderVal().isBlank() ?
										   "" :
										   itemCheckoutSearch.getOtherModalIdHeaderVal()
								   )
							   )
							   .data("showItem", itemCheckoutSearch.getShowItemCol())
							   .data("pagingCalculations", new PagingCalculations(output))
							   .data("storageService", this.getObjectService())
					   )
					   .type(MediaType.TEXT_HTML_TYPE);
				break;
			case MediaType.APPLICATION_JSON:
			default:
				log.debug("Requestor wanted json, or any other form");
		}
		
		return rb.build();
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
		@Context SecurityContext securityContext,
		@PathParam("id") String id
	) {
		return super.get(securityContext, id);
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
		@Context SecurityContext securityContext,
		@PathParam("id") String id,
		ObjectNode updates
	) {
		return super.update(securityContext, id, updates);
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
	@APIResponse(
		responseCode = "404",
		description = "No object found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ItemCheckout delete(
		@Context SecurityContext securityContext,
		@PathParam("id") String id
	) {
		return super.delete(securityContext, id);
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
			),
			@Content(
				mediaType = "text/html",
				schema = @Schema(type = SchemaType.STRING)
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
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response getHistoryForObject(
		@Context SecurityContext securityContext,
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject,
		@HeaderParam("accept") String acceptHeaderVal,
		@HeaderParam("searchFormId") String searchFormId
	) {
		return super.getHistoryForObject(securityContext, id, searchObject, acceptHeaderVal, searchFormId);
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
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@Context SecurityContext securityContext,
		@BeanParam HistorySearch searchObject
	) {
		return super.searchHistory(securityContext, searchObject);
	}
	
	//</editor-fold>
}
