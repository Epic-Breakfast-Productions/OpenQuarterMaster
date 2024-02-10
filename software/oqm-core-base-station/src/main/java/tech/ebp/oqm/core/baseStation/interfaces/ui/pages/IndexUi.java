package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class IndexUi {
	
	@GET
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public Response index() throws MalformedURLException, URISyntaxException {
		return Response.seeOther(
			UriBuilder.fromUri("/overview").build()
		).build();
	}
	
}
