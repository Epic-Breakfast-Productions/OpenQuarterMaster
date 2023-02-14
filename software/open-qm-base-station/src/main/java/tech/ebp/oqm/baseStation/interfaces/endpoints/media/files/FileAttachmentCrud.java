package tech.ebp.oqm.baseStation.interfaces.endpoints.media.files;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainFileObjectProvider;
import tech.ebp.oqm.baseStation.rest.file.FileAttachmentUploadBody;
import tech.ebp.oqm.baseStation.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.baseStation.service.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.lib.core.object.media.file.FileAttachment;
import tech.ebp.oqm.lib.core.rest.auth.roles.Roles;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Traced
@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/media/fileAttachments")
@Tags({@Tag(name = "File Attachments", description = "Endpoints for File Attachments.")})
@RequestScoped
public class FileAttachmentCrud extends MainFileObjectProvider<FileAttachment, FileAttachmentSearch, FileAttachmentUploadBody> {
	
	@Inject
	public FileAttachmentCrud(
		FileAttachmentService objectService,
		InteractingEntityService interactingEntityService,
		JsonWebToken jwt,
		@Location("tags/objView/history/searchResults.html")
		Template historyRowsTemplate
	) {
		super(
			objectService,
			interactingEntityService,
			jwt,
			historyRowsTemplate
		);
	}
	
	@POST
	@Operation(
		summary = "Adds a new file attachment."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
			schema = @Schema(
				implementation = ObjectId.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
//	@Override
	public Response add(
		@Context SecurityContext securityContext,
		@BeanParam FileAttachmentUploadBody body
	) throws IOException {
		FileAttachment newAttachmentObj = new FileAttachment();
		
		this.getObjectService().add(
			newAttachmentObj,
			body,
			this.getInteractingEntityFromJwt()
		);
		
		return Response.ok(newAttachmentObj.getId()).build();
	}
}
