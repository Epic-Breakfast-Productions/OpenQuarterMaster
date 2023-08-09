package tech.ebp.oqm.baseStation.rest.file;

import org.jboss.resteasy.reactive.PartType;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;

public class FileUploadBody {
	
	@FormParam("file")
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public InputStream file;
	
	@FormParam("fileName")
	@PartType(MediaType.TEXT_PLAIN)
	public String fileName;
}
