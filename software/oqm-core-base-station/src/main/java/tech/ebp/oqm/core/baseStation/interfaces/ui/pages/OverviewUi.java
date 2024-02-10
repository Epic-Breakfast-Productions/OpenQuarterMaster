package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.interfaces.ui.UiProvider;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientInfoHealthService;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class OverviewUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/overview")
	Template overview;
	
	@RestClient
	OqmCoreApiClientInfoHealthService coreApiClient;
	
	@Inject
	ExecutorService executorService;
	
	@GET
	@Path("overview")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response overview() {
		JsonNode itemCollectionStats;
		JsonNode storageCollectionStats;
		
		{
			Uni<ObjectNode> storageCollectionStatsUni = this.coreApiClient.getStorageBlockStats(this.getBearerHeaderStr());
			Uni<ObjectNode> itemCollectionStatsUni = this.coreApiClient.getItemStats(this.getBearerHeaderStr());
			
			storageCollectionStatsUni = storageCollectionStatsUni.runSubscriptionOn(this.executorService);
			itemCollectionStatsUni = itemCollectionStatsUni.runSubscriptionOn(this.executorService);
			
			storageCollectionStats = storageCollectionStatsUni.await().indefinitely();
			itemCollectionStats = itemCollectionStatsUni.await().indefinitely();
		}
		
		log.debug("Item stats json: {}", itemCollectionStats);
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(overview)
				.data("numItems", itemCollectionStats.get("size").asLong())
				.data("totalExpired", itemCollectionStats.get("numExpired").asLong())
//				.data("expiredList", inventoryItemService.list(Filters.gt("numExpired", 0), null, null))
				.data("totalExpiryWarn", itemCollectionStats.get("numCloseExpireWarn").asLong())
//				.data("expiredWarnList", inventoryItemService.list(Filters.gt("numExpiryWarn", 0), null, null))
				.data("totalLowStock", itemCollectionStats.get("numLowStock").asLong())
//				.data("lowStockList", inventoryItemService.list(Filters.gt("numLowStock", 0), null, null))
				.data("numStorageBlocks", storageCollectionStats.get("size").asLong())
//				.data("storageBlockService", storageBlockService)
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
}
