package tech.ebp.oqm.baseStation.interfaces.ui;

import io.opentracing.Tracer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.baseStation.rest.restCalls.KeycloakServiceCaller;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.baseStation.service.productLookup.ProductLookupService;
import tech.ebp.oqm.lib.core.object.interactingEntity.user.User;
import tech.ebp.oqm.lib.core.rest.user.UserGetResponse;
import tech.ebp.oqm.lib.core.units.UnitUtils;

import javax.annotation.security.PermitAll;
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
@Traced
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class HelpUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/help")
	Template overview;
	
	@Inject
	JsonWebToken jwt;
	
	@Inject
	@RestClient
	KeycloakServiceCaller ksc;
	
	@Inject
	Tracer tracer;
	
	@Inject
	UserService userService;
	
	@Inject
	ProductLookupService productLookupService;
	
	@GET
	@Path("help")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public Response overview(
		@Context SecurityContext securityContext,
		@CookieParam("jwt_refresh") String refreshToken
	) {
		logRequestContext(jwt, securityContext);
		
		User user = userService.getFromJwt(this.jwt);
		TemplateInstance template;
		if (user == null) {
			template = this.setupPageTemplate(overview, tracer)
						   .data("navbar", "toLogin");
		} else {
			template = this.setupPageTemplate(overview, tracer, UserGetResponse.builder(user).build())
						   .data("navbar", "full");
		}
		template = template
					   .data("unitCategoryMap", UnitUtils.UNIT_CATEGORY_MAP)
					   .data("productProviderInfoList", this.productLookupService.getProductProviderInfo())
					   .data("supportedPageScanInfoList", this.productLookupService.getSupportedPageScanInfo())
					   .data("legoProviderInfoList", this.productLookupService.getLegoProviderInfo())
		;
		
		List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(this.getUri(), refreshAuthToken(ksc, refreshToken));
		Response.ResponseBuilder responseBuilder = Response.ok(
			template,
			MediaType.TEXT_HTML_TYPE
		);
		
		if (newCookies != null && !newCookies.isEmpty()) {
			responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
		}
		
		return responseBuilder.build();
	}
	
}
