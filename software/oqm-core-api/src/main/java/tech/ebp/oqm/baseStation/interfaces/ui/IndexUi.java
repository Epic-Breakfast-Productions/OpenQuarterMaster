package tech.ebp.oqm.baseStation.interfaces.ui;


import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
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
@Path("/")
@Tags({@Tag(name = "UI")})
@ApplicationScoped
@Produces(MediaType.TEXT_HTML)
public class IndexUi {
	
	String indexLines = "";
	
	@Inject
	IndexUi(
		@Location("index.html")
		Template indexTemplate
	){
		this.indexLines = indexTemplate.render();
	}
	
	
	@GET
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public String getIndex() {
		return this.indexLines;
	}
}
