package tech.ebp.oqm.plugin.imageSearch.interfaces;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.ebp.oqm.plugin.imageSearch.service.ImageSearchService;

import java.io.IOException;

@RequestScoped
@Path("/imageSearch")
public class ImageSearch {
	
	@Inject
	ImageSearchService imageSearchService;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(
		@QueryParam("q") String queryImage
	) throws IOException {
		return Response.ok(this.imageSearchService.search(queryImage))
				   .build();
	}
}
