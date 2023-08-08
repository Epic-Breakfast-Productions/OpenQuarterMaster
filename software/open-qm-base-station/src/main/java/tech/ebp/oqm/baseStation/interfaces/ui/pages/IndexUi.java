package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.utils.AuthMode;
import tech.ebp.oqm.baseStation.model.validation.validators.PasswordConstraintValidator;

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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class IndexUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/index")
	Template index;
	@Inject
	@Location("webui/pages/accountCreate")
	Template accountCreate;
	
	@Context
	UriInfo uri;
	
	@Inject
	Span span;
	
	@ConfigProperty(name = "service.authMode")
	AuthMode authMode;
	
	@ConfigProperty(name = "service.externalAuth.interactionBase", defaultValue = "")
	String externInteractionBase;
	@ConfigProperty(name = "service.externalAuth.clientId", defaultValue = "")
	String externInteractionClientId;
	@ConfigProperty(name = "service.externalAuth.callbackPath", defaultValue = "")
	String externInteractionCallbackPath;
	
	@GET
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public Response index(
		@QueryParam("returnPath") String returnPath
	) throws MalformedURLException, URISyntaxException {
		//if logged in, skip to overview
		if(this.getInteractingEntity() != null){
			return Response.seeOther(
				UriBuilder.fromUri("/overview").build()
			).build();
		}
		
		Response.ResponseBuilder responseBuilder = Response.ok().type(MediaType.TEXT_HTML_TYPE);
		responseBuilder.entity(this.setupPageTemplate(index, span));
		
		return responseBuilder.build();
	}
	
	@GET
	@Path("/accountCreate")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance accountCreate() {
		return this.setupPageTemplate(accountCreate, span)
				   .data("firstUser", this.getInteractingEntityService().collectionEmpty())
				   .data("passwordHelpText", PasswordConstraintValidator.getPasswordRulesDescriptionHtml());
	}
}
