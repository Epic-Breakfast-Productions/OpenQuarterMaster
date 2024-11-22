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
		return this.processSearchResults(
			this.getOqmCoreApiClient()
				.invItemStoredSearch(this.getBearerHeaderStr(), this.getSelectedDb(), this.getItemId(), storedSearch)
				.call(results -> searchResultTweak.addStorageBlockLabelToSearchResult(results, this.getSelectedDb(), "storageBlock", this.getBearerHeaderStr()))
				.call(results -> searchResultTweak.addItemNameToSearchResult(results, this.getSelectedDb(), "item", this.getBearerHeaderStr()))
			,
			this.searchResultTemplate
				.data("showItem", showItem)
				.data("showStorage", showStorage)
			,
			acceptType,
			searchFormId,
			otherModalId,
			inputIdPrepend,
			actionType.orElse("select")
		);
	}

	@GET
	@Path("inBlock/{blockId}")
	@Operation(
		summary = "Searches all of an item's stored entries."
	)
	@APIResponse(
		responseCode = "200",
		description = "Stored entries retrieved."
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> searchInBlock(
		@BeanParam StoredSearch storedSearch,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId,
		@HeaderParam("otherModalId") String otherModalId,
		@HeaderParam("inputIdPrepend") String inputIdPrepend,
		@HeaderParam("showItem") boolean showItem,
		@HeaderParam("showStorage") boolean showStorage,
		@HeaderParam("actionType") Optional<String> actionType
	) {
		return this.processSearchResults(
			this.getOqmCoreApiClient()
				.invItemStoredSearch(this.getBearerHeaderStr(), this.getSelectedDb(), this.getItemId(), storedSearch)
				.call(results -> searchResultTweak.addStorageBlockLabelToSearchResult(results, this.getSelectedDb(), "storageBlock", this.getBearerHeaderStr()))
				.call(results -> searchResultTweak.addItemNameToSearchResult(results, this.getSelectedDb(), "item", this.getBearerHeaderStr()))
			,
			this.searchResultTemplate
				.data("showItem", showItem)
				.data("showStorage", showStorage)
			,
			acceptType,
			searchFormId,
			otherModalId,
			inputIdPrepend,
			actionType.orElse("select")
		);
	}

	@PUT
	@Path("/transact")
	@Operation(
		summary = "Applies a transaction to a stored item."
	)
	@APIResponse(
		responseCode = "200",
		description = "The id of the applied transaction record.",
		content = {
			@Content(
				mediaType = "application/json"
			)
		}
	)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<String> transact(
		ObjectNode transaction
	) throws Exception {
		log.info("Transacting item.");
		return this.getOqmCoreApiClient().invItemStoredTransact(this.getBearerHeaderStr(), this.getSelectedDb(), itemId, transaction);
	}

	@GET
	@Path("/transaction")
	@Operation(
		summary = "Searches all of an item's stored item transactions."
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
	public Uni<ObjectNode> searchTransactions(
		AppliedTransactionSearch appliedTransactionSearch
	) {
		return this.getOqmCoreApiClient().invItemStoredTransactionSearch(this.getBearerHeaderStr(), this.getSelectedDb(), itemId, appliedTransactionSearch);
	}

	@GET
	@Path("/transaction/{transactionId}")
	@Operation(
		summary = "Gets a particular applied transaction."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved."
	)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<ObjectNode> getAppliedTransaction(
		@PathParam("transactionId") String transactionId
	) {
		return this.getOqmCoreApiClient().invItemStoredTransactionGet(this.getBearerHeaderStr(), this.getSelectedDb(), itemId, transactionId);
	}

}
