package tech.ebp.oqm.plugin.imageSearch.interfaces;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.ebp.oqm.plugin.imageSearch.service.ImageSearchService;

@Path("/search")
public class ImageSearch {
    
    @Inject
    ImageSearchService imageSearchService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response search(
        @QueryParam("q") String queryImage
    ) {
        return Response.ok(
            this.imageSearchService.search(queryImage)
        ).build();
    }
}
