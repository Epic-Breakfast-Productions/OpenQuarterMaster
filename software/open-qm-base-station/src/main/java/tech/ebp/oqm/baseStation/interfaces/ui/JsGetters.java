package tech.ebp.oqm.baseStation.interfaces.ui;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

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
	
	@Inject
	@Location("webui/links.js")
	Template links;
	
	@Inject
	@Location("tags/carousel.html")
	Template carouselTemplate;
	String carouselLines = "";
	
	@Inject
	@Location("webui/carousel.js")
	Template carouselJs;
	
	@PostConstruct
	public void setup(){
		this.carouselLines = this.carouselTemplate
							.data("id", "")
							.render()
							.replaceAll("\n", "\\\\\n");
	}
	
	
	@GET
	@Path("icons.js")
	@PermitAll
	@Produces("text/javascript")
	public TemplateInstance icons() {
		return icons.instance();
	}
	
	@GET
	@Path("links.js")
	@PermitAll
	@Produces("text/javascript")
	public TemplateInstance links() {
		return links.instance();
	}
	
	@GET
	@Path("carousel.js")
	@PermitAll
	@Produces("text/javascript")
	public TemplateInstance carousel() {
		return this.carouselJs
				   .data("carouselLines", this.carouselLines);
	}
}
