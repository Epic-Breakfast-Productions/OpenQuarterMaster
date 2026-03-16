package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.inventory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.PassthroughProvider;
import tech.ebp.oqm.core.baseStation.service.modelTweak.SearchResultTweak;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.AppliedTransactionSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.HistorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StoredSearch;

import java.util.Optional;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/inventory/item/{itemId}/stored")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ItemStoredPassthrough extends PassthroughProvider {
	
	@Getter
	@Inject
	@Location("tags/search/itemStored/searchResults")
	Template searchResultTemplate;
	
	@Getter
	@PathParam("itemId")
	String itemId;
	
	@Inject
	SearchResultTweak searchResultTweak;
	
	@GET
	@Operation(
		summary = "Searches all of an item's stored entries."
	)
	@APIResponse(
		responseCode = "200",
		description = "Stored entries retrieved."
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> search(
		@BeanParam StoredSearch storedSearch,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId,
		@HeaderParam("otherModalId") String otherModalId,
		@HeaderParam("inputIdPrepend") String inputIdPrepend,
		@HeaderParam("showItem") boolean showItem,
		@HeaderParam("showStorage") boolean showStorage,
		@HeaderParam("actionType") Optional<String> actionType
	) {
		return this.handleCall(
			this.getOqmCoreApiClient()
				.invItemGet(this.getBearerHeaderStr(), this.getSelectedDb(), this.getItemId())
				.chain(
					(item)->{
						return this.processSearchResults(
							this.getOqmCoreApiClient()
								.invItemStoredSearch(this.getBearerHeaderStr(), this.getSelectedDb(), this.getItemId(), storedSearch)
								.call(results->searchResultTweak.addStorageBlockLabelToSearchResult(results, this.getSelectedDb(), "storageBlock", this.getBearerHeaderStr()))
								.call(results->searchResultTweak.addItemNameToSearchResult(results, this.getSelectedDb(), "item", this.getBearerHeaderStr()))
							,
							this.searchResultTemplate
								.data("showItem", showItem)
								.data("showStorage", showStorage)
								.data("showType", false)
								.data("inventoryItem", item)
							,
							acceptType,
							searchFormId,
							otherModalId,
							inputIdPrepend,
							actionType.orElse("select")
						);
					}
				)
		);
	}
	
	//	@GET //TODO if necessary
	//	@Path("history")
	//	@Operation(
	//		summary = "Searches all of an item's stored entries."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Stored entries retrieved."
	//	)
	//	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	//	public Uni<Response> searchHistory(
	//		@BeanParam StoredSearch storedSearch,
	//		@HeaderParam("Accept") String acceptType,
	//		@HeaderParam("searchFormId") String searchFormId,
	//		@HeaderParam("otherModalId") String otherModalId,
	//		@HeaderParam("inputIdPrepend") String inputIdPrepend,
	//		@HeaderParam("showItem") boolean showItem,
	//		@HeaderParam("showStorage") boolean showStorage,
	//		@HeaderParam("actionType") Optional<String> actionType
	//	) {
	//		return this.processSearchResults(
	//			this.getOqmCoreApiClient()
	//				.invItemStoredSearch(this.getBearerHeaderStr(), this.getSelectedDb(), this.getItemId(), storedSearch)
	//				.call(results -> searchResultTweak.addStorageBlockLabelToSearchResult(results, this.getSelectedDb(), "storageBlock", this.getBearerHeaderStr()))
	//				.call(results -> searchResultTweak.addItemNameToSearchResult(results, this.getSelectedDb(), "item", this.getBearerHeaderStr()))
	//			,
	//			this.searchResultTemplate
	//				.data("showItem", showItem)
	//				.data("showStorage", showStorage)
	//				.data("showType", false)
	//			,
	//			acceptType,
	//			searchFormId,
	//			otherModalId,
	//			inputIdPrepend,
	//			actionType.orElse("select")
	//		);
	//	}
	
	
	@GET
	@Path("{storedId}")
	@Operation(
		summary = "Gets an individual stored item object."
	)
	@APIResponse(
		responseCode = "200",
		description = "Stored entries retrieved."
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> getStored(
		@PathParam("storedId") String storedId
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().invItemStoredGet(
				this.getBearerHeaderStr(),
				this.getSelectedDb(),
				this.getItemId(),
				storedId
			)
		);
	}
	
	@PUT
	@Path("{storedId}")
	@Operation(
		summary = "Updates an individual stored item object."
	)
	@APIResponse(
		responseCode = "200",
		description = "Stored entries retrieved."
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> updateStored(
		@PathParam("storedId") String storedId,
		ObjectNode updates
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().invItemStoredUpdate(
				this.getBearerHeaderStr(),
				this.getSelectedDb(),
				this.getItemId(),
				storedId,
				updates
			)
		);
	}
	
	@GET
	@Path("{storedId}/history")
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
		@PathParam("storedId") String storedId,
		@BeanParam HistorySearch searchObject,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId
	) {
		Uni<ObjectNode> searchUni =
			this.getOqmCoreApiClient().invItemStoredSearchHistory(this.getBearerHeaderStr(), this.getSelectedDb(), this.getItemId(), storedId, searchObject);
		return this.handleCall(
			this.processHistoryResults(searchUni, acceptType, searchFormId)
		);
	}
}
