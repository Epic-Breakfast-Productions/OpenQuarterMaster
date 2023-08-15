package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.InteractingEntitySearch;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class UserAdminUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/userAdmin")
	Template userAdminTemplate;
	
	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	StorageBlockService storageBlockService;
	
	//TODO:: 361 instead, user search
//	@GET
//	@Path("userAdmin")
//	@RolesAllowed(Roles.USER_ADMIN)
//	@Produces(MediaType.TEXT_HTML)
//	public Response admin() throws URISyntaxException {
//		if (this.authMode != AuthMode.SELF) {
//			return Response.seeOther(new URI("/")).build();
//		}
//
//		InteractingEntitySearch search = new InteractingEntitySearch();
//		SearchResult<InteractingEntity> userResults = this.getInteractingEntityService().search(search, true);
//
//		search.getPagingOptions(true);
//		PagingCalculations pagingCalculations = new PagingCalculations(userResults);
//
//		Response.ResponseBuilder responseBuilder = Response.ok(
//			this.setupPageTemplate(userAdminTemplate, span, this.getInteractingEntity())
//				.data("showSearch", false)
//				.data("searchResults", userResults)
//				.data("pagingCalculations", pagingCalculations)
//				.data("selectableRolesMap", UserRoles.SELECTABLE_ROLES_DESC_MAP)
//				.data("searchObject", search)
//				.data("historySearchObject", new HistorySearch())
//			,
//			MediaType.TEXT_HTML_TYPE
//		);
//
//		return responseBuilder.build();
//	}
	
}
