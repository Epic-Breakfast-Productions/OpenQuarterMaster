package tech.ebp.oqm.core.api.model.rest.media.file;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.jboss.resteasy.reactive.PartType;

import java.io.InputStream;

public class FileUploadBody {
	
	@Parameter(description = "The file content to upload")
	@FormParam("file")
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public InputStream file;
	
	@Parameter(description = "The name of the file")
	@FormParam("fileName")
	@PartType(MediaType.TEXT_PLAIN)
	public String fileName;
	
	@Parameter(description = "The source of the file (user, system, url, etc.)")
	@FormParam("source")
	@DefaultValue("")
	@PartType(MediaType.TEXT_PLAIN)
	public String source;
	
	@Parameter(description = "The description of the file.")
	@DefaultValue("")
	@FormParam("description")
	@PartType(MediaType.TEXT_PLAIN)
	public String description;
}
