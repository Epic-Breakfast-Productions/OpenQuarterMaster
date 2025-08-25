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
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/inventory/item/{itemId}/stored")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ItemTransactionPassthrough extends PassthroughProvider {
	
	@Getter
	@Inject
	@Location("tags/search/itemStored/searchResults")
	Template searchResultTemplate;
	
	@Getter
	@PathParam("itemId")
	String itemId;
	
	@Inject
	SearchResultTweak searchResultTweak;
	
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
	public Uni<Response> transact(
		ObjectNode transaction
	) throws Exception {
		log.info("Transacting item.");
		return this.handleCall(
			this.getOqmCoreApiClient().invItemStoredTransact(this.getBearerHeaderStr(), this.getSelectedDb(), itemId, transaction)
		);
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
	public Uni<Response> searchTransactions(
		AppliedTransactionSearch appliedTransactionSearch
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().invItemStoredTransactionSearch(this.getBearerHeaderStr(), this.getSelectedDb(), itemId, appliedTransactionSearch)
		);
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
	public Uni<Response> getAppliedTransaction(
		@PathParam("transactionId") String transactionId
	) {
		return this.handleCall(
			this.getOqmCoreApiClient().invItemStoredTransactionGet(this.getBearerHeaderStr(), this.getSelectedDb(), itemId, transactionId)
		);
	}
}
