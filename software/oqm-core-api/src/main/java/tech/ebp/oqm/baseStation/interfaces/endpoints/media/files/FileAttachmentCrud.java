package tech.ebp.oqm.baseStation.interfaces.endpoints.media.files;

// TODO:: reenable once working #51

import com.mongodb.client.ClientSession;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
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
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainFileObjectProvider;
import tech.ebp.oqm.baseStation.model.object.media.file.FileAttachment;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.baseStation.rest.file.FileAttachmentUploadBody;
import tech.ebp.oqm.baseStation.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.baseStation.service.mongo.file.FileAttachmentService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.service.mongo.utils.FileContentsGet;

import java.io.IOException;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/media/fileAttachments")
@Tags({@Tag(name = "File Attachments", description = "Endpoints for File Attachments.")})
@RequestScoped
public class FileAttachmentCrud extends MainFileObjectProvider<FileAttachment, FileAttachmentSearch, FileAttachmentGet, FileAttachmentUploadBody> {
	
	@Getter
	@Inject
	FileAttachmentService fileObjectService;
	
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
		@BeanParam FileAttachmentUploadBody body
	) throws IOException {
		FileAttachment newAttachmentObj = new FileAttachment();
		newAttachmentObj.setFileName(body.fileName);
		newAttachmentObj.setDescription(body.description);
		
		this.getFileObjectService().add(
			newAttachmentObj,
			body,
			this.getInteractingEntity()
		);
		
		return Response.ok(this.getFileObjectService().fileObjToGet(newAttachmentObj)).build();
	}
	
	@GET
	@Operation(
		summary = "Searches for file attachments."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
			schema = @Schema(
				type = SchemaType.ARRAY,
				implementation = FileAttachmentGet.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Produces(MediaType.APPLICATION_JSON)
	//	@Override
	public Response search(
		@BeanParam FileAttachmentSearch search
	) {
		return super.search(search);
	}
	
	@GET
	@Path("{id}")
	@Operation(
		summary = "Gets a particular file attachment details."
	)
	@APIResponse(
		responseCode = "200",
		description = "File information retrieved.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
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
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	//	@Override
	public Response get(
		@PathParam("id")
		String id
	) throws IOException {
		return Response.ok(
			FileAttachmentGet.fromFileAttachment(
				this.getFileObjectService().getFileObjectService().get(id),
				this.getFileObjectService().getRevisions(new ObjectId(id))
			)
		).build();
	}
	
	@GET
	@Path("{id}/data")
	@Operation(
		summary = "Gets the data for the latest revision of a file."
	)
	@APIResponse(
		responseCode = "200",
		description = "File information retrieved.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
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
	@RolesAllowed(Roles.INVENTORY_EDIT)
	//	@Override
	public Response getLatestData(
		@PathParam("id")
		String id
	) throws IOException {
		FileContentsGet fileContentsGet = this.getFileObjectService().getLatestFile(id);
		Response.ResponseBuilder response = Response.ok(
			fileContentsGet.getContents()
		);
		response.header("Content-Type", fileContentsGet.getMetadata().getMimeType());
		response.header("Content-Disposition", "attachment;filename=" + fileContentsGet.getMetadata().getOrigName());
		response.header("hash-md5", fileContentsGet.getMetadata().getHashes().getMd5());
		response.header("hash-sha1", fileContentsGet.getMetadata().getHashes().getSha1());
		response.header("hash-sha256", fileContentsGet.getMetadata().getHashes().getSha256());
		response.header("upload-datetime", fileContentsGet.getMetadata().getUploadDateTime());
		return response.build();
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Adds a new revision to the file."
	)
	@APIResponse(
		responseCode = "200",
		description = "File updated.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
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
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	//	@Override
	public Response update(
		@PathParam("id")
		String id,
		@BeanParam FileAttachmentUploadBody body
	) throws IOException {
		this.getFileObjectService().updateFile(
			new ObjectId(id),
			body,
			this.getInteractingEntity()
		);
		
		return Response.ok(
			FileAttachmentGet.fromFileAttachment(
				this.getFileObjectService().getFileObjectService().get(id),
				this.getFileObjectService().getRevisions(new ObjectId(id))
			)
		).build();
	}
	
	@DELETE
	@Path("{id}")
	@Operation(
		summary = "Deletes a particular file attachment."
	)
	@APIResponse(
		responseCode = "200",
		description = "File information retrieved.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
			schema = @Schema(
				implementation = FileAttachment.class
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
	public Response delete(
		@PathParam("id")
		String id
	) throws IOException {
		return Response.ok(
			this.getFileObjectService().removeFile(null, new ObjectId(id), this.getInteractingEntity())
		).build();
	}
}
