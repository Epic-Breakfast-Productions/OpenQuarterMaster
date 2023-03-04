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
import tech.ebp.oqm.baseStation.rest.restCalls.KeycloakServiceCaller;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.InventoryItemSearch;
import tech.ebp.oqm.baseStation.rest.search.ItemListSearch;
import tech.ebp.oqm.baseStation.service.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.ItemListService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.lib.core.object.interactingEntity.user.User;
import tech.ebp.oqm.lib.core.object.itemList.ItemList;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.rest.auth.roles.Roles;
import tech.ebp.oqm.lib.core.rest.user.UserGetResponse;

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
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

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
	UserService userService;
	@Inject
	ItemListService itemListService;
	@Inject
	InteractingEntityService interactingEntityService;
	
	@Inject
	JsonWebToken jwt;
	@Inject
	@RestClient
	KeycloakServiceCaller ksc;
	
	@Inject
	Span span;
	
	@GET
	@Path("itemLists")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response itemLists(
		@Context SecurityContext securityContext,
		@BeanParam ItemListSearch itemListSearch,
		@CookieParam("jwt_refresh") String refreshToken
	) {
		logRequestContext(jwt, securityContext);
		User user = userService.getFromJwt(this.jwt);
		List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(this.getUri(), refreshAuthToken(ksc, refreshToken));
		
		SearchResult<ItemList> searchResults = this.itemListService.search(itemListSearch, true);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(
					this.itemLists,
					span,
					UserGetResponse.builder(user).build(),
					searchResults
				)
				.data("searchObject", itemListSearch)
				.data("historySearchObject", new HistorySearch())
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		if (newCookies != null && !newCookies.isEmpty()) {
			responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
		}
		
		return responseBuilder.build();
	}
	
	@GET
	@Path("itemList/{id}")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response itemList(
		@Context SecurityContext securityContext,
		@CookieParam("jwt_refresh") String refreshToken,
		@PathParam("id") String listId
	) {
		logRequestContext(jwt, securityContext);
		User user = userService.getFromJwt(this.jwt);
		List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(this.getUri(), refreshAuthToken(ksc, refreshToken));
		
		ItemList list = this.itemListService.get(listId);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(
					this.itemList,
					span,
					UserGetResponse.builder(user).build()
				)
				.data("itemList", list)
				.data("listCreateEvent", this.itemListService.getCreateEvent(list.getId()))
				.data("interactingEntityService", this.interactingEntityService)
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		if (newCookies != null && !newCookies.isEmpty()) {
			responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
		}
		
		return responseBuilder.build();
	}
}
