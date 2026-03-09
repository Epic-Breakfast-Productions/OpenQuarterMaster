package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.inventory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.PassthroughProvider;
import tech.ebp.oqm.core.baseStation.service.modelTweak.SearchResultTweak;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.HistorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ItemCheckoutSearch;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/inventory/item-checkout")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ItemCheckoutPassthrough extends PassthroughProvider {
	
	@Getter
	@Inject
	@Location("tags/search/itemCheckout/searchResults")
	Template searchResultTemplate;
	
	@Inject
	SearchResultTweak searchResultTweak;
	
	@GET
	@Operation(
		summary = "Gets a list of storage blocks, using search parameters."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json"
			)
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> search(
		@BeanParam ItemCheckoutSearch itemCheckoutSearch,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId,
		@HeaderParam("otherModalId") String otherModalId,
		@HeaderParam("inputIdPrepend") String inputIdPrepend,
		@HeaderParam("showItem") String showItem,
		@HeaderParam("actionType") String actionType
	) {
		return this.handleCall(
			this.processSearchResults(
				this.getOqmCoreApiClient().itemCheckoutSearch(this.getBearerHeaderStr(), this.getSelectedDb(), itemCheckoutSearch)
					.call(results->searchResultTweak.addStorageBlockLabelToSearchResult(results, this.getSelectedDb(), "fromBlock", this.getBearerHeaderStr()))
					.call(results->searchResultTweak.addItemNameToSearchResult(results, this.getSelectedDb(), "item", this.getBearerHeaderStr()))
					.call(results->searchResultTweak.addCreatedByInteractingEntityRefToCheckoutSearchResult(results, this.getSelectedDb(), this.getBearerHeaderStr()))
					.call(results->searchResultTweak.addInteractingEntityRefToCheckoutSearchResult(results, this.getBearerHeaderStr()))
				,
				this.searchResultTemplate.data("showItem", "true".equalsIgnoreCase(showItem)),
				acceptType,
				searchFormId,
				otherModalId,
				inputIdPrepend,
				actionType
			)
		);
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
			mediaType = "application/json"
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
	public Uni<Response> get(
		@PathParam("id") String id
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().itemCheckoutGet(this.getBearerHeaderStr(), this.getSelectedDb(), id)
		);
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
			mediaType = "application/json"
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
	public Uni<Response> update(
		@PathParam("id") String id,
		ObjectNode updates
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().itemCheckoutUpdate(this.getBearerHeaderStr(), this.getSelectedDb(), id, updates)
		);
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
				mediaType = "application/json"
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
	public Uni<Response> getHistoryForObject(
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId
	) {
		Uni<ObjectNode> searchUni = this.getOqmCoreApiClient().itemCheckoutGetHistoryForObject(this.getBearerHeaderStr(), this.getSelectedDb(), id, searchObject);
		return this.handleCall(
			this.processHistoryResults(searchUni, acceptType, searchFormId)
		);
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
				mediaType = "application/json"
			)
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().itemCheckoutSearchHistory(this.getBearerHeaderStr(), this.getSelectedDb(), searchObject)
		);
	}
	
}
