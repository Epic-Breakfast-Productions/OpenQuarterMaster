package tech.ebp.oqm.core.api.interfaces.endpoints.media.files;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.MainFileObjectProvider;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.model.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;

import java.io.IOException;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1_DB_AWARE + "/media/fileAttachment")
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
		description = "Files searched for."
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = APPLICATION_JSON)
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = APPLICATION_JSON)
	)
	@Produces(APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<FileAttachmentGet> search(
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
		description = "Object added. Returns the metadata of the created file."
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = APPLICATION_JSON)
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(APPLICATION_JSON)
	public FileAttachmentGet add(
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
	@Produces(APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public FileAttachmentGet get(
		@PathParam("id") ObjectId id
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
	@Produces(APPLICATION_JSON)
	public Integer updateFile(
		@PathParam("id") ObjectId id,
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
		description = "Object updated."
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
	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	public FileAttachmentGet updateObj(
		@PathParam("id")
		ObjectId id,
		@Schema(type = SchemaType.OBJECT, implementation = FileAttachment.class, description = "Partial object updates; supply all or some of values to update.")
		ObjectNode updates
	) {
		return this.getFileService().fileObjToGet(
			this.getOqmDbIdOrName(),
			this.getFileService().getFileObjectService().update(
				this.getOqmDbIdOrName(),
				null,
				id,
				updates,
				this.getInteractingEntity()
			)
		);
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
	@Produces(APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public FileMetadata getRevision(
		@PathParam("id")
		ObjectId id,
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
		headers = {
			@Header(name = "Content-Disposition", description = "Denotes the original filename as the name of the file"),
			@Header(name = "hash-md5", description = "The MD5 hash of the file"),
			@Header(name = "hash-sha1", description = "The SHA1 hash of the file"),
			@Header(name = "hash-sha256", description = "The SHA256 hash of the file"),
			@Header(name = "upload-datetime", description = "When the file was uploaded")
		}
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
	public Response getRevisionData(
		@PathParam("id")
		ObjectId id,
		@Parameter(description = "The revision number of the file. Can also use \"latest\" and \"first\"")
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
		description = "File deleted."
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
	@Produces(APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	public FileAttachmentGet remove(
		@PathParam("id")
		ObjectId id
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
		description = "Object history searched."
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
	@Produces(APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Override
	public SearchResult<ObjectHistoryEvent> getHistoryForObject(
		@PathParam("id") ObjectId id,
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
		description = "History Searched."
	)
	@Produces(APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Override
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		return super.searchHistory(searchObject);
	}
}
