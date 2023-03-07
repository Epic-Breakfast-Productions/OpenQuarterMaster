package tech.ebp.oqm.baseStation.interfaces.ui;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Blocking
@Slf4j
@Path("/res/js/")
@Tags({@Tag(name = "UI")})
@ApplicationScoped
@Produces(MediaType.TEXT_HTML)
public class JsGetters {
	
	@Inject
	@Location("webui/icons.js")
	Template icons;
	
	@GET
	@Path("icons.js")
	@PermitAll
	@Produces("text/javascript")
	public TemplateInstance icons() {
		return icons.instance();
	}
}
