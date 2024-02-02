package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.baseStation.interfaces.ui.UiProvider;
import tech.ebp.oqm.core.baseStation.utils.Roles;

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
	
	@GET
	@Path("overview")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response overview() {
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(overview)
//				.data("numItems", inventoryItemService.count())
//				.data("totalExpired", inventoryItemService.getNumStoredExpired())
//				.data("expiredList", inventoryItemService.list(Filters.gt("numExpired", 0), null, null))
//				.data("totalExpiryWarn", inventoryItemService.getNumStoredExpiryWarn())
//				.data("expiredWarnList", inventoryItemService.list(Filters.gt("numExpiryWarn", 0), null, null))
//				.data("totalLowStock", inventoryItemService.getNumLowStock())
//				.data("lowStockList", inventoryItemService.list(Filters.gt("numLowStock", 0), null, null))
//				.data("numStorageBlocks", storageBlockService.count())
//				.data("storageBlockService", storageBlockService)
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
}