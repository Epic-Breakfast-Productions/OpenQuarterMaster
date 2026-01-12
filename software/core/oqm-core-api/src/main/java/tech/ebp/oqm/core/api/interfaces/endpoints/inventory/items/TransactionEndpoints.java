package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.search.AppliedTransactionSearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.transactions.AppliedTransactionService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1_DB_AWARE + "/inventory/item/{itemId}/stored/transaction")
@Tags({@Tag(name = "Inventory Items", description = "Endpoints for inventory item CRUD, and managing stored items.")})
@RequestScoped
public class TransactionEndpoints extends MainObjectProvider<Stored, StoredSearch> {
	
	@Getter
	@Inject
	StoredService objectService;
	
	@Getter
	@Inject
	InventoryItemService inventoryItemService;
	
	@Getter
	@Inject
	AppliedTransactionService appliedTransactionService;
	
	@Getter
	Class<Stored> objectClass = Stored.class;
	
	@Getter
	@PathParam("itemId")
	String itemId;
	
	private InventoryItem inventoryItem;
	
	public InventoryItem getInventoryItem() {
		if (this.inventoryItem == null) {
			this.inventoryItem = this.inventoryItemService.get(this.getOqmDbIdOrName(), this.itemId);
		}
		return this.inventoryItem;
	}
	
	@POST
	@Operation(
		summary = "Applies a transaction to a stored item."
	)
	@APIResponse(
		responseCode = "200",
		description = "The id of the applied transaction record.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					type = SchemaType.OBJECT,
					implementation = AppliedTransaction.class
				)
			)
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public AppliedTransaction transact(@Valid ItemStoredTransaction transaction) throws Exception {
		return this.appliedTransactionService.apply(this.getOqmDbIdOrName(), null, this.getInventoryItem(), transaction, this.getInteractingEntity());
	}
	
	@GET
	@Operation(
		summary = "Searches all of an item's stored item transactions."
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
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<AppliedTransaction> searchTransactions(
		@BeanParam AppliedTransactionSearch storedSearch
	) {
		return this.appliedTransactionService.search(this.getOqmDbIdOrName(), storedSearch);
	}
	
	@GET
	@Path("{transactionId}")
	@Operation(
		summary = "Gets a particular applied transaction."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved."
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public AppliedTransaction getAppliedTransaction(
		@PathParam("transactionId") String transactionId
	) {
		return this.getAppliedTransactionService().get(this.getOqmDbIdOrName(), transactionId);
	}
}
