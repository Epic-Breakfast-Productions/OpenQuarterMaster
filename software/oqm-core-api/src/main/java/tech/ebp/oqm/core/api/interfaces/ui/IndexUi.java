package tech.ebp.oqm.core.api.interfaces.ui;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@SuppressWarnings("LombokGetterMayBeUsed")
@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI", description = "UI endpoints")})
@PermitAll
@Produces(MediaType.TEXT_HTML)
@ApplicationScoped
public class IndexUi {
	
	@Getter
	String indexLines;
	
	//TODO: determine if we want to do this
//	@Inject
//	SmallRyeHealthReporter healthReporter;
	
	@Inject
	IndexUi(
		@Location("index.html")
		Template indexTemplate
	){
		this.indexLines = indexTemplate.render();
	}
	
	@GET
	@Operation(summary = "Simple index content to lead user to more resources. Same as /index.html.")
	public String getRoot() {
		return this.getIndexLines();
	}
	
	//Probably overthinking this
	@GET
	@Operation(summary = "Simple index content to lead user to more resources. Same as / .")
	@Path("index.html")
	public String getIndex() {
		return this.getIndexLines();
	}
}
