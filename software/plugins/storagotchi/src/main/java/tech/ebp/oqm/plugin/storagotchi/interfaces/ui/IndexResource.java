package tech.ebp.oqm.plugin.storagotchi.interfaces.ui;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class IndexResource {
	
	@Location("index")
	Template index;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance hello() {
		return this.index.instance();
	}
}
