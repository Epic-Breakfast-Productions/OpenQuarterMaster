package tech.ebp.oqm.core.api.interfaces.ui;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import java.util.Optional;

@SuppressWarnings("LombokGetterMayBeUsed")
@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI", description = "UI endpoints")})
@PermitAll
@Produces(MediaType.TEXT_HTML)
@ApplicationScoped
public class IndexUi {

	//TODO: determine if we want to do this
//	@Inject
//	SmallRyeHealthReporter healthReporter;

	@Getter
	@HeaderParam("x-forwarded-prefix")
	Optional<String> forwardedPrefix;

	@Getter
	@Inject
	@Location("index.html")
	Template indexTemplate;

	
	//Probably overthinking this
	@GET
	@Path("index.html")
	@Operation(summary = "Simple index content to lead user to more resources. Same as / .")
	public TemplateInstance getIndex() {
		return this.getIndexTemplate()
			.data("rootPrefix", forwardedPrefix.orElse(""));
	}

	@GET
	@Operation(summary = "Simple index content to lead user to more resources. Same as /index.html.")
	public TemplateInstance getRoot() {
		return this.getIndex();
	}
}
