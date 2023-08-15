package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.ItemListSearch;
import tech.ebp.oqm.baseStation.service.mongo.ItemCategoryService;
import tech.ebp.oqm.baseStation.service.mongo.ItemListService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.model.object.itemList.ItemList;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ItemListsUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/itemLists")
	Template itemLists;
	
	@Inject
	@Location("webui/pages/itemList")
	Template itemList;
	
	@Inject
	ItemListService itemListService;
	@Inject
	ItemCategoryService itemCategoryService;
	
	@GET
	@Path("itemLists")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response itemLists(
		@BeanParam ItemListSearch itemListSearch
	) {
		
		SearchResult<ItemList> searchResults = this.itemListService.search(itemListSearch, true);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(
					this.itemLists,
					this.getInteractingEntity(),
					searchResults
				)
				.data("searchObject", itemListSearch)
				.data("historySearchObject", new HistorySearch())
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
	@GET
	@Path("itemList/{id}")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response itemList(
		@PathParam("id") String listId
	) {
		ItemList list = this.itemListService.get(listId);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(
					this.itemList,
					this.getInteractingEntity()
				)
				.data("itemList", list)
				.data("allowedUnitsMap", UnitUtils.UNIT_CATEGORY_MAP)
				.data("itemCatsService", this.itemCategoryService)
				.data("listCreateEvent", this.itemListService.getCreateEvent(list.getId()))
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
}
