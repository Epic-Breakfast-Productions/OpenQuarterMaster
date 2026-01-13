package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
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
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1_DB_AWARE + "/inventory/item/{itemId}/block/{blockId}")
@Tags({@Tag(name = "Inventory Items", description = "Endpoints for inventory item CRUD, and managing stored items.")})
@RequestScoped
public class InBlockEndpoints extends MainObjectProvider<Stored, StoredSearch> {

	@Getter
	@Inject
	StoredService objectService;
	
	@Getter
	@Inject
	InventoryItemService inventoryItemService;
	
	@Getter
	@Inject
	StorageBlockService storageBlockService;

	@Getter
	Class<Stored> objectClass = Stored.class;

	@Getter
	@PathParam("itemId")
	String itemId;
	
	@Getter
	@PathParam("blockId")
	String blockId;

	private InventoryItem inventoryItem;
	private StorageBlock storageBlock;
	
	private StorageBlock getStorageBlock() {
		if (this.storageBlock == null) {
			this.storageBlock = this.storageBlockService.get(this.getOqmDbIdOrName(), this.blockId);
			
		}
		return this.storageBlock;
	}
	
	private InventoryItem getInventoryItem() {
		if (this.inventoryItem == null) {
			this.inventoryItem = this.inventoryItemService.get(this.getOqmDbIdOrName(), this.itemId);
			
			if(!this.inventoryItem.getStorageBlocks().contains(this.getStorageBlock().getId())){
				throw new NotFoundException("Storage block given not found in the given item");
			}
		}
		return this.inventoryItem;
	}
	
	private Stored applyDefaults(Stored stored){
		stored.applyDefaultsFromItem(this.getInventoryItem());
		return stored;
	}
	
	private SearchResult<Stored> applyDefaults(SearchResult<Stored> searchResult){
		searchResult.getResults().forEach(this::applyDefaults);
		return searchResult;
	}
	
	@GET
	@Path("stored")
	@Operation(
		summary = "Gets a list of items stored in the storage block, using search parameters."
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
	public SearchResult<Stored> search(
		@BeanParam StoredSearch storedSearch
	) {
		return this.applyDefaults(super.search(storedSearch));
	}
}
