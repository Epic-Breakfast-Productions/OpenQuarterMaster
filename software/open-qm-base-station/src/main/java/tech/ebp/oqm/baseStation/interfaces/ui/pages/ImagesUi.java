package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.ImageSearch;
import tech.ebp.oqm.baseStation.service.mongo.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.model.object.media.Image;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ImagesUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/images")
	Template images;
	
	@Inject
	ImageService imageService;
	
	@Inject
	Span span;
	
	@GET
	@Path("/images")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response images(
		@BeanParam ImageSearch imageSearch
	) {
		SearchResult<Image> searchResults = this.imageService.search(imageSearch, true);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(
					images,
					span,
					this.getInteractingEntity(),
					searchResults
				)
				.data("searchObject", imageSearch)
				.data("historySearchObject", new HistorySearch()),
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
}
