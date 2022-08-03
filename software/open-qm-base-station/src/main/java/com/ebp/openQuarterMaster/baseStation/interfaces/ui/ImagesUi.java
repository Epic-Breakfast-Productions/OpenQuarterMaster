package com.ebp.openQuarterMaster.baseStation.interfaces.ui;

import com.ebp.openQuarterMaster.baseStation.rest.restCalls.KeycloakServiceCaller;
import com.ebp.openQuarterMaster.baseStation.rest.search.ImageSearch;
import com.ebp.openQuarterMaster.baseStation.service.mongo.ImageService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.lib.core.media.Image;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
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

import javax.annotation.security.PermitAll;
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
public class ImagesUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/images")
	Template images;
	
	@Inject
	ImageService imageService;
	
	@Inject
	UserService userService;
	
	@Inject
	JsonWebToken jwt;
	@Inject
	@RestClient
	KeycloakServiceCaller ksc;
	
	@Inject
	Tracer tracer;
	
	
	@GET
	@Path("/images")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public Response images(
		@Context SecurityContext securityContext,
		@CookieParam("jwt_refresh") String refreshToken,
		@BeanParam ImageSearch imageSearch
	) {
		logRequestContext(jwt, securityContext);
		User user = userService.getFromJwt(this.jwt);
		List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(refreshAuthToken(ksc, refreshToken));
		
		SearchResult<Image> searchResults = this.imageService.search(imageSearch, true);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(
				images,
				tracer,
				UserGetResponse.builder(user).build(),
				searchResults,
				imageSearch.getPagingOptions(true)
			),
			MediaType.TEXT_HTML_TYPE
		);
		
		if (newCookies != null && !newCookies.isEmpty()) {
			responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
		}
		
		return responseBuilder.build();
	}
}
