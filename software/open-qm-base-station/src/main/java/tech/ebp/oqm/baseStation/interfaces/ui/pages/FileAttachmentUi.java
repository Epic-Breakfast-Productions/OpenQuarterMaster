package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.model.object.media.Image;
import tech.ebp.oqm.baseStation.model.object.media.file.FileAttachment;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.baseStation.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.ImageSearch;
import tech.ebp.oqm.baseStation.service.mongo.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class FileAttachmentUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/fileAttachments")
	Template images;
	
	@Inject
	FileAttachmentService fileAttachmentService;
	
	@GET
	@Path("/files")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response images(
		@BeanParam FileAttachmentSearch fileAttachmentSearch
	) {
		SearchResult<FileAttachmentGet> searchResults = this.fileAttachmentService.searchToGet(
			this.fileAttachmentService.getFileObjectService().search(fileAttachmentSearch, true)
		);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(
					images,
					this.getInteractingEntity(),
					searchResults
				)
				.data("searchObject", fileAttachmentSearch)
				.data("historySearchObject", new HistorySearch()),
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
}
