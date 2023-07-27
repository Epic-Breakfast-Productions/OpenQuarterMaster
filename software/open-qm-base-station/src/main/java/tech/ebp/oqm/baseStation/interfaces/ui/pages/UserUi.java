package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.baseStation.rest.restCalls.KeycloakServiceCaller;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.service.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.user.UserGetResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

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
	UserService userService;
	@Inject
	InteractingEntityService interactingEntityService;
	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	StorageBlockService storageBlockService;
	
	@Inject
	JsonWebToken jwt;
	
	@Inject
	@RestClient
	KeycloakServiceCaller ksc;
	
	@Inject
	Span span;
	
	@GET
	@Path("you")
	@RolesAllowed(Roles.USER)
	@Produces(MediaType.TEXT_HTML)
	public Response overview(
		@Context SecurityContext securityContext,
		@CookieParam("jwt_refresh") String refreshToken
	) {
		logRequestContext(jwt, securityContext);
		User user = userService.getFromJwt(this.jwt);
		UserGetResponse ugr = UserGetResponse.builder(user).build();
		List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(
			this.getUri(),
			refreshAuthToken(ksc, refreshToken)
		);
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(overview, span, ugr)
				.data("user", ugr)
				.data("userService", userService)
				.data("interactingEntityService", interactingEntityService)
				.data("numItems", inventoryItemService.count())
				.data("numStorageBlocks", storageBlockService.count())
				.data("historySearchObject", new HistorySearch())
				.data("jwt", this.jwt.getRawToken())
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		if (newCookies != null && !newCookies.isEmpty()) {
			responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
		}
		
		return responseBuilder.build();
	}
	
}
