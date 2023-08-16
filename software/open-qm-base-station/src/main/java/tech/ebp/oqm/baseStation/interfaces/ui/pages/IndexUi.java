package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.oidc.OidcSession;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.model.validation.validators.PasswordConstraintValidator;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
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
	
	@Context
	UriInfo uri;
	
	@GET
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public Response index() throws MalformedURLException, URISyntaxException {
		//if logged in, skip to overview
		if(this.getInteractingEntity() != null){
			return Response.seeOther(
				UriBuilder.fromUri("/overview").build()
			).build();
		}
		
		Response.ResponseBuilder responseBuilder = Response.ok().type(MediaType.TEXT_HTML_TYPE);
		responseBuilder.entity(this.setupPageTemplate(index));
		
		return responseBuilder.build();
	}
	
}
