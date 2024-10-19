package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.AppliedTransactionService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1_DB_AWARE + "/inventory/item/{itemId}/stored")
@Tags({@Tag(name = "Inventory Items", description = "Endpoints for inventory item CRUD, and managing stored items.")})
@RequestScoped
public class StoredEndpoints extends MainObjectProvider<Stored, StoredSearch> {

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

	@Getter
	private InventoryItem inventoryItem;

	@PostConstruct
	public void setup(){
		this.inventoryItem = this.inventoryItemService.get(this.getOqmDbIdOrName(), this.itemId);
	}

	@GET
	@Operation(
		summary = "Searches all of an item's stored entries."
	)
	@APIResponse(
		responseCode = "200",
		description = "Stored entries retrieved."
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<Stored> search(
		@BeanParam StoredSearch storedSearch
	) {
		return super.search(storedSearch);
	}

}
