package tech.ebp.oqm.baseStation.rest.file;

import org.jboss.resteasy.reactive.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

public class FileUploadBody {
	
	@FormParam("file")
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public InputStream file;
	
	@FormParam("fileName")
	@PartType(MediaType.TEXT_PLAIN)
	public String fileName;
}
