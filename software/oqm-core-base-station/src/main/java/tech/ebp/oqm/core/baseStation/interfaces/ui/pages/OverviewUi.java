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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.core.baseStation.utils.Searches;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;

import java.util.Map;
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
	
	@Inject
	ExecutorService executorService;
	
	@GET
	@Path("overview")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Uni<Response> overview() {
		return this.getUni(
			Map.of(
				"itemCollectionStats", this.coreApiClient.invItemCollectionStats(this.getBearerHeaderStr(), this.getSelectedDb()),
				"storageCollectionStats", this.coreApiClient.storageBlockCollectionStats(this.getBearerHeaderStr(), this.getSelectedDb()),
				"parentBlocks", this.coreApiClient.storageBlockSearch(this.getBearerHeaderStr(), this.getSelectedDb(), Searches.BLOCK_PARENT_SEARCH),
				"expiredResults", this.coreApiClient.invItemSearch(this.getBearerHeaderStr(), this.getSelectedDb(), Searches.ITEM_EXPIRED_SEARCH),
				"expiryWarnResults", this.coreApiClient.invItemSearch(this.getBearerHeaderStr(), this.getSelectedDb(), Searches.ITEM_EXPIRY_WARN_SEARCH),
				"lowStockResults", this.coreApiClient.invItemSearch(this.getBearerHeaderStr(), this.getSelectedDb(), Searches.ITEM_LOW_STOCK_SEARCH)
			)
		);
	}
	
}
