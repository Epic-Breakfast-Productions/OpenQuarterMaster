package tech.ebp.oqm.plugin.imageSearch.interfaces;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.model.search.SearchResults;
import tech.ebp.oqm.plugin.imageSearch.service.ImageSearchService;

import java.io.IOException;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Path("/imageSearch")
public class ImageSearchEndpoint {
	
	@Inject
	ImageSearchService imageSearchService;
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(APPLICATION_JSON)
	public SearchResults search(
		@BeanParam ImageSearch query
	) throws IOException {
		return this.imageSearchService.search(query);
	}
}
