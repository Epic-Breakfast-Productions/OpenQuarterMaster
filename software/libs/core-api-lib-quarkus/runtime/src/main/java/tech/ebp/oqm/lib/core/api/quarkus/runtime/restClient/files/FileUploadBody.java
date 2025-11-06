package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jboss.resteasy.reactive.PartType;

import java.io.InputStream;

@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileUploadBody {
	
	@NonNull
	@FormParam("file")
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public InputStream file;
	
	@NonNull
	@FormParam("fileName")
	@PartType(MediaType.TEXT_PLAIN)
	public String fileName;
	
	@NonNull
	@FormParam("source")
	@PartType(MediaType.TEXT_PLAIN)
	public String source;
	
	@NonNull
	@FormParam("description")
	@PartType(MediaType.TEXT_PLAIN)
	public String description;
}
