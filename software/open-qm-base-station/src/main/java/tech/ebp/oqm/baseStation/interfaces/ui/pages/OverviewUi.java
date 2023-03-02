package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import com.mongodb.client.model.Filters;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentracing.Tracer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.baseStation.rest.restCalls.KeycloakServiceCaller;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.lib.core.object.interactingEntity.user.User;
import tech.ebp.oqm.lib.core.rest.auth.roles.Roles;
import tech.ebp.oqm.lib.core.rest.user.UserGetResponse;

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

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class OverviewUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/overview")
	Template overview;
	
	@Inject
	UserService userService;
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
	
	@WithSpan
	@GET
	@Path("overview")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response overview(
		@Context SecurityContext securityContext,
		@CookieParam("jwt_refresh") String refreshToken
	) {
		logRequestContext(jwt, securityContext);
		User user = userService.getFromJwt(this.jwt);
		List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(this.getUri(), refreshAuthToken(ksc, refreshToken));
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(overview, span, UserGetResponse.builder(user).build())
				.data("numItems", inventoryItemService.count())
				.data("totalExpired", inventoryItemService.getNumStoredExpired())
				.data("expiredList", inventoryItemService.list(Filters.gt("numExpired", 0), null, null))
				.data("totalExpiryWarn", inventoryItemService.getNumStoredExpiryWarn())
				.data("expiredWarnList", inventoryItemService.list(Filters.gt("numExpiryWarn", 0), null, null))
				.data("totalLowStock", inventoryItemService.getNumLowStock())
				.data("lowStockList", inventoryItemService.list(Filters.gt("numLowStock", 0), null, null))
				.data("numStorageBlocks", storageBlockService.count())
				.data("storageBlockService", storageBlockService),
			MediaType.TEXT_HTML_TYPE
		);
		
		if (newCookies != null && !newCookies.isEmpty()) {
			responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
		}
		
		return responseBuilder.build();
	}
	
}
