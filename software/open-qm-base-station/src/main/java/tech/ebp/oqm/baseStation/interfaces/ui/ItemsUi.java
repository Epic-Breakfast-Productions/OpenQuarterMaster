package tech.ebp.oqm.baseStation.interfaces.ui;

import io.opentracing.Tracer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.baseStation.rest.restCalls.KeycloakServiceCaller;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingOptions;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;
import tech.ebp.oqm.baseStation.service.mongo.search.SortType;
import tech.ebp.oqm.baseStation.utils.UserRoles;
import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StoredType;
import tech.ebp.oqm.lib.core.object.user.User;
import tech.ebp.oqm.lib.core.rest.user.UserGetResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Traced
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ItemsUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/items")
	Template items;
	
	@Inject
	UserService userService;
	@Inject
	InventoryItemService inventoryItemService;
	
	@Inject
	JsonWebToken jwt;
	@Inject
	@RestClient
	KeycloakServiceCaller ksc;
	
	@Inject
	Tracer tracer;
	
	@GET
	@Path("items")
	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response items(
		@Context SecurityContext securityContext,
		//for actual queries
		@QueryParam("name") String name,
		@QueryParam("keywords") List<String> keywords,
		@QueryParam("storedType") StoredType storedType,
		//paging
		@QueryParam("pageSize") Integer pageSize,
		@QueryParam("pageNum") Integer pageNum,
		//sorting
		@QueryParam("sortBy") String sortField,
		@QueryParam("sortType") SortType sortType,
		@CookieParam("jwt_refresh") String refreshToken
	) {
		logRequestContext(jwt, securityContext);
		User user = userService.getFromJwt(this.jwt);
		List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(refreshAuthToken(ksc, refreshToken));
		
		
		Bson sort = SearchUtils.getSortBson(sortField, sortType);
		PagingOptions pageOptions = PagingOptions.fromQueryParams(pageSize, pageNum, false);
		
		SearchResult<InventoryItem> searchResults = this.inventoryItemService.search(
			name,
			keywords,
			storedType,
			sort,
			pageOptions
		);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(items, tracer, UserGetResponse.builder(user).build(), searchResults, pageOptions)
				.data("allowedUnitsMap", UnitUtils.ALLOWED_UNITS_MAP),
			MediaType.TEXT_HTML_TYPE
		);
		
		if (newCookies != null && !newCookies.isEmpty()) {
			responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
		}
		
		return responseBuilder.build();
	}
	
}
