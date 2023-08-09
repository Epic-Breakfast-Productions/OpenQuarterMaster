package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.baseStation.rest.printouts.PageOrientation;
import tech.ebp.oqm.baseStation.rest.printouts.PageSizeOption;
import tech.ebp.oqm.baseStation.rest.search.CategoriesSearch;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.service.mongo.ItemCategoryService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.object.storage.ItemCategory;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class CategoriesUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/categories")
	Template categories;
	
	@Inject
	ItemCategoryService itemItemCategoryService;
	
	@Inject
	Span span;
	
	@GET
	@Path("categories")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response categories(
		@BeanParam CategoriesSearch categoriesSearch
	) {
		SearchResult<ItemCategory> searchResults = this.itemItemCategoryService.search(categoriesSearch, true);
		this.itemItemCategoryService.listIterator();
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(categories, span, this.getInteractingEntity(), searchResults)
				.data("allowedUnitsMap", UnitUtils.UNIT_CATEGORY_MAP)
				.data("numCategories", itemItemCategoryService.count())
				.data("itemCatService", itemItemCategoryService)
				.data("searchObject", categoriesSearch)
				.data("pageOrientationOptions", PageOrientation.values())
				.data("pageSizeOptions", PageSizeOption.values())
				.data("historySearchObject", new HistorySearch()),
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
}
