package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class UserUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/you")
	Template overview;
	
	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	StorageBlockService storageBlockService;
	
	@Inject
	Span span;
	
	@GET
	@Path("you")
	@RolesAllowed(Roles.USER)
	@Produces(MediaType.TEXT_HTML)
	public Response user() {
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(overview, span, this.getInteractingEntity())
				.data("numItems", inventoryItemService.count())
				.data("numStorageBlocks", storageBlockService.count())
				.data("historySearchObject", new HistorySearch())
				.data("jwt", this.getJwt().getRawToken())
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
}
