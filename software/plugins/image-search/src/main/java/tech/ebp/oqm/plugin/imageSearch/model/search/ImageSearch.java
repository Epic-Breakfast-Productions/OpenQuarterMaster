package tech.ebp.oqm.plugin.imageSearch.model.search;


import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.jboss.resteasy.reactive.PartType;

import java.io.InputStream;

public class ImageSearch {
	
	@Parameter(description = "The file content to upload")
	@FormParam("file")
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public InputStream file;
	
	@Parameter(description = "The name of the file")
	@FormParam("fileName")
	@PartType(MediaType.TEXT_PLAIN)
	public String fileName;
	
	@Parameter(description = "The max number of results to return.")
	@FormParam("maxResults")
	@DefaultValue("10")
	@PartType(MediaType.TEXT_PLAIN)
	public Integer maxResults;
	
	@Parameter(description = "The threshold of how similar to identify with.")
	@FormParam("maxResults")
	@DefaultValue("75.0")
	@PartType(MediaType.TEXT_PLAIN)
	public Double threshold;
}
