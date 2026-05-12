package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class IndexUi {
	
	@ConfigProperty(name = "ui.defaults.indexRedirect", defaultValue = "/overview")
	URI redirectPath;
	
	@GET
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public Response index() throws MalformedURLException, URISyntaxException {
		URI uri = this.redirectPath;
		
		log.info("User navigated to index. Redirecting to: {}", uri);
		
		return Response.seeOther(uri).build();
	}
}
