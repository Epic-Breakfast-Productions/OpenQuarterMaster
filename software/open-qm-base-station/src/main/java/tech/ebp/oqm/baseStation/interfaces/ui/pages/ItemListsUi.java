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

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

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
	
	@Inject
	Span span;
	
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
					span,
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
					span,
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
