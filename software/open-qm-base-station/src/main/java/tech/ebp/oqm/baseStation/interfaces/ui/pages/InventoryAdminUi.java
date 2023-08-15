package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.unit.custom.NewDerivedCustomUnitRequest;
import tech.ebp.oqm.baseStation.model.units.UnitCategory;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;
import tech.ebp.oqm.baseStation.model.units.ValidUnitDimension;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class InventoryAdminUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/inventoryAdmin")
	Template inventoryAdminTemplate;
	
	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	StorageBlockService storageBlockService;
	@Inject
	CustomUnitService customUnitService;
	
	@GET
	@Path("inventoryAdmin")
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Produces(MediaType.TEXT_HTML)
	public Response inventoryAdmin() {
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(inventoryAdminTemplate, this.getInteractingEntity())
				.data("unitDerivisionTypes", NewDerivedCustomUnitRequest.DeriveType.values())
				.data("unitDimensions", ValidUnitDimension.values())
				.data("unitCategories", UnitCategory.values())
				.data("allowedUnitsMap", UnitUtils.UNIT_CATEGORY_MAP)
				.data("customUnits", this.customUnitService.list())
				.data("historySearchObject", new HistorySearch())
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
}
