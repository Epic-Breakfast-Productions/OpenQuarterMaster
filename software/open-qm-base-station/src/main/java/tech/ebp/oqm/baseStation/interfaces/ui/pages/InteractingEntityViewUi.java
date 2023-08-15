package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.config.BaseStationInteractingEntity;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class InteractingEntityViewUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/entity")
	Template overview;
	
	@Inject
	BaseStationInteractingEntity baseStationInteractingEntity;
	
	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	StorageBlockService storageBlockService;
	
	@GET
	@Path("entityView/{id}")
	@RolesAllowed("user")
	@Produces(MediaType.TEXT_HTML)
	public Response extEntityView(
		@PathParam("id") String entityId
	) {
		InteractingEntity entity = this.getInteractingEntityService().get(entityId);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(overview, this.getInteractingEntity())
				.data("entity", entity)
				.data("numItems", inventoryItemService.count())
				.data("numStorageBlocks", storageBlockService.count())
				.data("historySearchObject", new HistorySearch())
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
	@GET
	@Path("entityView/baseStation")
	@RolesAllowed("user")
	@Produces(MediaType.TEXT_HTML)
	public Response baseStationView(
		@Context SecurityContext securityContext,
		@CookieParam("jwt_refresh") String refreshToken
	) {
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(overview, this.getInteractingEntity())
				.data("entity", this.baseStationInteractingEntity)
				.data("numItems", inventoryItemService.count())
				.data("numStorageBlocks", storageBlockService.count())
				.data("historySearchObject", new HistorySearch())
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
}
