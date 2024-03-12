package tech.ebp.oqm.baseStation.interfaces.endpoints.media.files;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainFileObjectProvider;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.media.FileMetadata;
import tech.ebp.oqm.baseStation.model.object.media.file.FileAttachment;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.baseStation.rest.file.FileUploadBody;
import tech.ebp.oqm.baseStation.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.service.mongo.utils.FileContentsGet;

import java.io.IOException;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/media/fileAttachment")
@Tags({@Tag(name = "File Attachments", description = "Endpoints for File Attachments.")})
@RequestScoped
public class FileAttachmentCrud extends MainFileObjectProvider<FileAttachment, FileUploadBody, FileAttachmentSearch, FileAttachmentGet> {
	
	@Getter
	@Inject
	FileAttachmentService fileService;
	
	@Override
	protected FileAttachment getFileObjFromUpload(FileUploadBody upload) {
		return new FileAttachment(
			upload.fileName,
			upload.description,
			upload.source
		);
	}
	
	@GET
	@Operation(
		summary = "Searches for files"
	)
	@APIResponse(
		responseCode = "200",
		description = "Searched.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = SearchResult.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public Response search(
		@BeanParam FileAttachmentSearch searchObject
	) {
		return super.search(searchObject);
	}
	
	@POST
	@Operation(
		summary = "Adds a file."
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
	public Response add(
		@BeanParam FileUploadBody body
	) throws IOException {
		return super.add(body);
	}
	
	@Path("{id}")
	@GET
	@Operation(
		summary = "Gets a particular file object, not the file itself."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = FileAttachmentGet.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public FileAttachmentGet get(
		@PathParam("id") String id
	) {
		return super.get(id);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular File, adds a new revision.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = Integer.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@WithSpan
	public Integer updateFile(
		@PathParam("id") String id,
		@BeanParam FileUploadBody body
	) throws IOException {
		return super.updateFile(id, body);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular file's Object.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = Integer.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@WithSpan
	public FileAttachmentGet updateObj(
		@PathParam("id")
		String id,
		ObjectNode updates
	) {
		return this.getFileService().fileObjToGet(this.getFileService().getFileObjectService().update(
			id,
			updates,
			this.getInteractingEntity()
		));
	}
	
	@Path("{id}/revision/{rev}")
	@GET
	@Operation(
		summary = "Gets a particular file revision's metadata."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = FileMetadata.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public FileMetadata getRevision(
		@PathParam("id")
		String id,
		@PathParam("rev")
		String revision
	) throws IOException {
		return super.getRevision(id, revision);
	}
	
	@Path("{id}/revision/{rev}/data")
	@GET
	@Operation(
		summary = "Gets a particular file revision's data."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = MainObject.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces("*/*")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public Response getRevisionData(
		@PathParam("id")
		String id,
		@PathParam("rev")
		String revision
	) throws IOException {
		return super.getRevisionData(id, revision);
	}
	
	@Path("{id}")
	@DELETE
	@Operation(
		summary = "Removes a particular file."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = MainObject.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@WithSpan
	public FileAttachmentGet remove(
		@PathParam("id")
		String id
	) {
		return super.remove(id);
	}
	
	@GET
	@Path("{id}/history")
	@Operation(
		summary = "Gets a particular file's history."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(type = SchemaType.ARRAY, implementation = ObjectHistoryEvent.class)
			)
		}
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "No history found for object with that id.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	@Override
	public Response getHistoryForObject(
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject
	) {
		return super.getHistoryForObject(id, searchObject);
	}
	
	@GET
	@Path("history")
	@Operation(
		summary = "Searches the history for the objects."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					implementation = SearchResult.class
				)
			)
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	@Override
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		return super.searchHistory(searchObject);
	}
}
