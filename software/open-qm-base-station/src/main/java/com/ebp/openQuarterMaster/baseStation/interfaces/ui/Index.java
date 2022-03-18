package com.ebp.openQuarterMaster.baseStation.interfaces.ui;

import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.UUID;

import static com.ebp.openQuarterMaster.baseStation.utils.AuthMode.EXTERNAL;

@Traced
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class Index extends UiProvider {
	
	@Inject
	@Location("webui/pages/index")
	Template index;
	@Inject
	@Location("webui/pages/accountCreate")
	Template accountCreate;
	
	@Inject
	JsonWebToken jwt;
	
	@ConfigProperty(name = "service.authMode")
	AuthMode authMode;
	
	@ConfigProperty(name = "service.externalAuth.interactionBase", defaultValue = "")
	String externInteractionBase;
	@ConfigProperty(name = "service.externalAuth.clientId", defaultValue = "")
	String externInteractionClientId;
	@ConfigProperty(name = "service.externalAuth.callbackUrl", defaultValue = "")
	String externInteractionCallbackUrl;
	
	@GET
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public Response index(
		@Context SecurityContext securityContext,
		@QueryParam("returnPath") String returnPath
	) throws MalformedURLException, URISyntaxException {
		logRequestContext(jwt, securityContext);
		
		String redirectUri = externInteractionCallbackUrl;
		
		if (returnPath != null && !returnPath.isBlank()) {
			redirectUri = new URIBuilder(redirectUri).addParameter("returnPath", returnPath).build().toString();
		}
		
		Response.ResponseBuilder responseBuilder = Response.ok().type(MediaType.TEXT_HTML_TYPE);
		
		
		if (EXTERNAL.equals(this.authMode)) {
			URIBuilder signInLinkBuilder = new URIBuilder(this.externInteractionBase + "/auth");
			String state = UUID.randomUUID().toString();
			
			signInLinkBuilder.setParameter("response_type", "code");
			signInLinkBuilder.setParameter("scope", "openid");
			signInLinkBuilder.setParameter("audience", "account");
			signInLinkBuilder.setParameter("state", state);
			signInLinkBuilder.setParameter("client_id", externInteractionClientId);
			signInLinkBuilder.setParameter("redirect_uri", redirectUri);
			
			responseBuilder.entity(
				this.setupPageTemplate(index)
					.data("signInLink", signInLinkBuilder.build())
			).cookie(
				UiUtils.getNewCookie("externState", state, "For verification or return.", UiUtils.DEFAULT_COOKIE_AGE)
			);
		} else {
			responseBuilder.entity(this.setupPageTemplate(index));
		}
		
		return responseBuilder.build();
	}
	
	@GET
	@Path("/accountCreate")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance accountCreate(
		@Context SecurityContext securityContext
	) {
		logRequestContext(jwt, securityContext);
		return this.setupPageTemplate(accountCreate);
	}
}
