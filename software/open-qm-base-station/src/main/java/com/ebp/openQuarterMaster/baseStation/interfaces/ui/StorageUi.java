package com.ebp.openQuarterMaster.baseStation.interfaces.ui;

import com.ebp.openQuarterMaster.baseStation.rest.restCalls.KeycloakServiceCaller;
import com.ebp.openQuarterMaster.baseStation.rest.search.StorageBlockSearch;
import com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.opentracing.Tracer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
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

@Traced
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class StorageUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/storage")
	Template storage;
	
	@Inject
	UserService userService;
	
	@Inject
	StorageBlockService storageBlockService;
	
	@Inject
	JsonWebToken jwt;
	@Inject
	@RestClient
	KeycloakServiceCaller ksc;
	
	@Inject
	Tracer tracer;
	
	@GET
	@Path("storage")
	@RolesAllowed("user")
	@Produces(MediaType.TEXT_HTML)
	public Response storage(
		@Context SecurityContext securityContext,
		@CookieParam("jwt_refresh") String refreshToken,
		@BeanParam StorageBlockSearch storageBlockSearch
	) {
		logRequestContext(jwt, securityContext);
		User user = userService.getFromJwt(this.jwt);
		List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(refreshAuthToken(ksc, refreshToken));
		
		SearchResult<StorageBlock> searchResults = this.storageBlockService.search(storageBlockSearch, true);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(storage, tracer, UserGetResponse.builder(user).build(), searchResults,
								   storageBlockSearch.getPagingOptions(true)
				)
				.data("allowedUnitsMap", UnitUtils.ALLOWED_UNITS_MAP)
				.data("numStorageBlocks", storageBlockService.count())
				.data("storageService", storageBlockService),
			MediaType.TEXT_HTML_TYPE
		);
		
		if (newCookies != null && !newCookies.isEmpty()) {
			responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
		}
		
		return responseBuilder.build();
	}
	
}
