package tech.ebp.oqm.baseStation.rest.file;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;

public class FileAttachmentUploadBody extends FileUploadBody {
	@FormParam("description")
	@PartType(MediaType.TEXT_PLAIN)
	public String description;

}
