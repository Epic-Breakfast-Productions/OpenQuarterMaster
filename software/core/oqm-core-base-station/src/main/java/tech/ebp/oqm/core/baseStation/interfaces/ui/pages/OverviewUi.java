package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.service.modelTweak.SearchResultTweak;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.core.baseStation.utils.Searches;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ItemCategorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StoredSearch;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class OverviewUi extends UiProvider {
	
	@Getter
	@Inject
	@Location("webui/pages/overview")
	Template pageTemplate;
	
	@RestClient
	OqmCoreApiClientService coreApiClient;
	
	@Getter
	@ConfigProperty(name="ui.overview.storageTree.defaultPageSize")
	int storageTreeDefaultPageSize;
	
	@Getter
	@ConfigProperty(name="ui.overview.lowStockItems.defaultPageSize")
	int lowStockItemsDefaultPageSize;
	
	@Getter
	@ConfigProperty(name="ui.overview.expiringItems.defaultPageSize")
	int expiringDefaultPageSize;
	@Getter
	@ConfigProperty(name="ui.overview.expiredItems.defaultPageSize")
	int expiredDefaultPageSize;
	@Inject
	SearchResultTweak searchResultTweak;
	
	@GET
	@Path("overview")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Uni<Response> overview(
		@QueryParam("storageBlockTreePage") Optional<Integer> storageBlockPage,
		@QueryParam("lowStockPage") Optional<Integer> lowStockPage,
		@QueryParam("expiringPage") Optional<Integer> expiringPage,
		@QueryParam("expiredPage") Optional<Integer> expiredPage
	) {
		StorageBlockSearch treeBlockSearch = Searches.BLOCK_PARENT_SEARCH.toBuilder()
												 .pageSize(this.getStorageTreeDefaultPageSize())
												 .pageNum(storageBlockPage.orElse(1))
												 .build();
		InventoryItemSearch lowStockSearch = Searches.ITEM_LOW_STOCK_SEARCH.toBuilder()
												 .pageSize(this.getLowStockItemsDefaultPageSize())
												 .pageNum(lowStockPage.orElse(1))
												 .build();
		StoredSearch expiringSearch = Searches.ITEM_STORED_EXPIRY_WARN_SEARCH.toBuilder()
												 .pageSize(this.getExpiringDefaultPageSize())
												 .pageNum(expiringPage.orElse(1))
												 .build();
		StoredSearch expiredSearch = Searches.ITEM_STORED_EXPIRED_SEARCH.toBuilder()
												 .pageSize(this.getExpiredDefaultPageSize())
												 .pageNum(expiredPage.orElse(1))
												 .build();
		
		return this.getUni(
			Map.of(
				"itemCollectionStats", this.coreApiClient.invItemCollectionStats(this.getBearerHeaderStr(), this.getSelectedDb()),
				"storageCollectionStats", this.coreApiClient.storageBlockCollectionStats(this.getBearerHeaderStr(), this.getSelectedDb()),
				"parentBlocks", this.coreApiClient.storageBlockSearch(this.getBearerHeaderStr(), this.getSelectedDb(), treeBlockSearch),
				"expiredResults", this.coreApiClient.invItemStoredSearch(this.getBearerHeaderStr(), this.getSelectedDb(), expiredSearch)
									  .call(results->searchResultTweak.addStorageBlockLabelToSearchResult(results, this.getSelectedDb(), "storageBlock", this.getBearerHeaderStr()))
									  .call(results->searchResultTweak.addItemNameToSearchResult(results, this.getSelectedDb(), "item", this.getBearerHeaderStr())),
				"expiryWarnResults", this.coreApiClient.invItemStoredSearch(this.getBearerHeaderStr(), this.getSelectedDb(), expiringSearch)
										 .call(results->searchResultTweak.addStorageBlockLabelToSearchResult(results, this.getSelectedDb(), "storageBlock", this.getBearerHeaderStr()))
										 .call(results->searchResultTweak.addItemNameToSearchResult(results, this.getSelectedDb(), "item", this.getBearerHeaderStr())),
				"lowStockResults", this.coreApiClient.invItemSearch(this.getBearerHeaderStr(), this.getSelectedDb(), lowStockSearch),
				"currency", this.coreApiClient.getCurrency(this.getBearerHeaderStr()),
				"allUnitMap", this.coreApiClient.unitGetAll(this.getBearerHeaderStr()),
				"allCategorySearchResults", this.coreApiClient.itemCatSearch(this.getBearerHeaderStr(), this.getSelectedDb(), new ItemCategorySearch())
				)
		);
	}
	
}
