package tech.ebp.oqm.plugin.imageSearch.model.search;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import lombok.Builder;
import lombok.ToString;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.jboss.resteasy.reactive.PartType;
import tech.ebp.oqm.plugin.imageSearch.model.Model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@ToString
@Builder
public class ImageSearch {

	@Parameter(description = "The database we are concerning ourselves with.")
	@PathParam("oqmDbIdOrName")
	public String oqmDbIdOrName;

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
	@Builder.Default
	public Integer maxResults = 10;

	@Parameter(description = "The threshold of how similar to identify with.")
	@FormParam("maxResults")
	@DefaultValue("75.0")
	@Min(50) @Max(100)
	@PartType(MediaType.TEXT_PLAIN)
	@Builder.Default
	public Double threshold = 75.0;

	@Parameter(description = "The models to use to identify the given image.")
	@FormParam("models")
	@Builder.Default
	public List<Model> models = List.of();
}
