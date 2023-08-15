package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.ItemCheckoutSearch;
import tech.ebp.oqm.baseStation.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
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
public class ItemCheckoutUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/itemCheckout")
	Template overview;
	
	@Inject
	ItemCheckoutService itemCheckoutService;
	
	@Inject
	Span span;
	
	@GET
	@Path("itemCheckout")
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Produces(MediaType.TEXT_HTML)
	public Response itemCheckout(
		@BeanParam ItemCheckoutSearch itemCheckoutSearch
	) {
		SearchResult<ItemCheckout> searchResults = this.itemCheckoutService.search(itemCheckoutSearch, true);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(overview, this.getInteractingEntity(), searchResults)
				.data("searchObject", itemCheckoutSearch)
				.data("showItem", true)
				.data("showStillCheckedOut", true)
				.data("historySearchObject", new HistorySearch())
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
}
