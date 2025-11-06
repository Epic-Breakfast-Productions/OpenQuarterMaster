package tech.ebp.oqm.core.api.interfaces.ui;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import java.text.MessageFormat;
import java.util.Optional;

@SuppressWarnings("LombokGetterMayBeUsed")
@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI", description = "UI endpoints")})
@PermitAll
@Produces(MediaType.TEXT_HTML)
@RequestScoped
public class IndexUi {

	//TODO: determine if we want to do this
//	@Inject
//	SmallRyeHealthReporter healthReporter;

	@Getter
	@HeaderParam("x-forwarded-prefix")
	Optional<String> forwardedPrefix;
	
	@ConfigProperty(name="service.version")
	String serviceVersion;

	@GET
	@Path("index.html")
	@Produces(MediaType.TEXT_HTML)
	@Operation(summary = "Simple index content to lead user to more resources. Same as / .")
	public String getIndex() {
		return MessageFormat.format("""
<html lang="en">
<head>
	<title>OQM API</title>
</head>
<body>
<main>
	<img src="{0}/media/logo.svg" alt="OQM Logo">
	<h1>
		OQM Core API
	</h1>
	<p>
		This service serves the core api forming the base functionality of the Open QuarterMaster system.
	</p>
	<p>
		See <a href="{0}/q/swagger-ui/">Swagger</a> for API documentation.
	</p>
</main>
<hr />
<footer>
	<p>
		Version {1}
	</p>
	<p>
		&copy; 2024 <a href="https://epic-breakfast-productions.tech">EBP</a>
	</p>
</footer>
</body>
</html>
""",
			this.forwardedPrefix.orElse(""),
			serviceVersion
		);
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Operation(summary = "Simple index content to lead user to more resources. Same as /index.html.")
	public String getRoot() {
		return this.getIndex();
	}
}
