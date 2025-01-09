package tech.ebp.oqm.plugin.alertMessenger.interfaces.ui;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class Home extends UiInterface {

	@Getter
	@Inject
	@Location("webui/pages/index")
	Template pageTemplate;

	@GET
	@RolesAllowed("inventoryView")
	@Produces(MediaType.TEXT_HTML)
	public Response index() {
		log.info("Got index page");
		return Response.ok(
			this.setupPageTemplate(this.pageTemplate)
		).build();
	}

}
