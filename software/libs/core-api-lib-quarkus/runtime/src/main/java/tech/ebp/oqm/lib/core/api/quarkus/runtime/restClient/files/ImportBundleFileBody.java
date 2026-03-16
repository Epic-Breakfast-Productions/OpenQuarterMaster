package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;

import java.io.InputStream;

public class ImportBundleFileBody {
	
	@FormParam("file")
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public InputStream file;
	
	@FormParam("fileName")
	@PartType(MediaType.TEXT_PLAIN)
	public String fileName;
	
	@FormParam("options")
	@PartType(MediaType.APPLICATION_JSON)
	public String options;
}
