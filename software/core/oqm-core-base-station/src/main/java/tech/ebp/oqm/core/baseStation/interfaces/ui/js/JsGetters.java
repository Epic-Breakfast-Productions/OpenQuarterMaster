package tech.ebp.oqm.core.baseStation.interfaces.ui.js;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import java.util.Optional;

/**
 * Serves the shared JavaScript resources consumed by the Web UI.
 *
 * <p>Each endpoint renders a Qute template from {@code src/main/resources/templates/webui/js/}
 * as a {@code text/javascript} response, allowing the templates to embed server-provided values
 * (such as the application's root prefix) before delivery to the browser.</p>
 *
 * <p>Example usage from a Web UI page template:</p>
 * <pre>
 *   <script src="{rootPrefix}/res/js/constants.js"></script>
 *   import { Icons } from '{rootPrefix}/res/js/Icons.js';
 * </pre>
 */
@Slf4j
@Path("/res/js/")
@Tags({@Tag(name = "JS Utilities")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class JsGetters {

	/**
	 * Reverse-proxy base path (if any) under which the application is mounted, as reported by the
	 * proxy via the {@code x-forwarded-prefix} header.
	 */
	@Getter
	@HeaderParam("x-forwarded-prefix")
	Optional<String> forwardedPrefix;

	/**
	 * Returns the application's root path prefix (e.g. {@code "/oqm"} when proxied under a
	 * sub-path), or an empty string when the application is served from the domain root.
	 * Injected into templates that build resource URLs for the browser.
	 */
	protected String getRootPrefix() {
		return this.forwardedPrefix.orElse("");
	}

	@Inject
	@Location("webui/js/Icons.js")
	Template icons;

	@Inject
	@Location("webui/js/constants.js")
	Template constants;

	@Inject
	@Location("webui/js/Carousel.js")
	Template carouselJs;

	@Inject
	@Location("webui/js/PageComponents.js")
	Template componentsJs;

	@GET
	@Path("constants.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> constants() {
		return constants.data("rootPrefix", this.getRootPrefix()).createUni();
	}

	@GET
	@Path("Icons.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> icons() {
		return icons.instance().createUni();
	}

	@GET
	@Path("Carousel.js")
	@PermitAll
	@Produces("text/javascript")
	public TemplateInstance carousel() {
		return this.carouselJs.instance();
	}

	@GET
	@Path("PageComponents.js")
	@PermitAll
	@Produces("text/javascript")
	public TemplateInstance components() {
		return this.componentsJs.instance();
	}
}
