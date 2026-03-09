package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.inventory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
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
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/inventory/item/{itemId}/block/{blockId}")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ItemInBlockPassthrough extends PassthroughProvider {
	
	@Getter
	@Inject
	@Location("tags/search/itemStored/searchResults")
	Template searchResultTemplate;
	
	@Getter
	@PathParam("itemId")
	String itemId;
	@Getter
	@PathParam("blockId")
	String blockId;
	
	@Inject
	SearchResultTweak searchResultTweak;
	
	@GET
	@Path("stored")
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
		return this.handleCall(
			this.processSearchResults(
				this.getOqmCoreApiClient()
					.invItemStoredInBlockSearch(this.getBearerHeaderStr(), this.getSelectedDb(), this.getItemId(), this.getBlockId(), storedSearch)
					.call(results->searchResultTweak.addStorageBlockLabelToSearchResult(results, this.getSelectedDb(), "storageBlock", this.getBearerHeaderStr()))
					.call(results->searchResultTweak.addItemNameToSearchResult(results, this.getSelectedDb(), "item", this.getBearerHeaderStr()))
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
			)
		);
	}
	
}
