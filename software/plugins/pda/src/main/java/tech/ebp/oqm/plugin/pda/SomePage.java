package tech.ebp.oqm.plugin.pda;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;

import static java.util.Objects.requireNonNull;

@RequestScoped
@Path("/")
public class SomePage {

	@Getter
	@Inject
	@Location("page.qute.html")
	Template pageTemplate;

	@Getter
	@Inject
	@Location("manifest.qute.json")
	Template manifest;
	
	@Getter
	@Inject
	@Location("serviceWorker.qute.js")
	Template serviceWorker;


	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance get(@QueryParam("name") String name) {
		return this.getPageTemplate().data("name", name);
	}

	@GET
	@Path("/manifest.json")
	@Produces(MediaType.APPLICATION_JSON)
	public TemplateInstance getManifestJson() {
		return this.getManifest().instance();
	}

	@GET
	@Path("/sw.js")
	@Produces("text/javascript")
	public TemplateInstance getServiceWorkerJs() {
		return this.getServiceWorker().instance();
	}

}
