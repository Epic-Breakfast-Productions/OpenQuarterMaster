package tech.ebp.oqm.core.api.interfaces.endpoints.media;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
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
import tech.ebp.oqm.core.api.interfaces.endpoints.MainFileObjectProvider;
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.media.ImageGet;
import tech.ebp.oqm.core.api.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.core.api.model.rest.storage.IMAGED_OBJ_TYPE_NAME;
import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.ImageSearch;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.ItemCategoryService;
import tech.ebp.oqm.core.api.service.mongo.MongoObjectService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1_DB_AWARE + "/media/image")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@RequestScoped
public class ImageCrud extends MainFileObjectProvider<Image, FileUploadBody, ImageSearch, ImageGet> {
	
	private static final URI EMPTY_IMAGE_URI;
	
	static {
		try {
			EMPTY_IMAGE_URI = new URI("/media/empty.svg");
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Inject
	StorageBlockService storageBlockService;
	@Inject
	InventoryItemService itemService;
	@Inject
	ItemCategoryService itemCategoryService;
	@Inject
	StoredService itemStoredService;
	@Inject
	Validator validator;
	
	@Inject
	@Getter
	ImageService fileService;
	
	@Getter
	Class<Image> objectClass = Image.class;
	
	
	@Override
	protected Image getFileObjFromUpload(FileUploadBody upload) {
		return new Image(
			upload.fileName,
			upload.description,
			upload.source
		);
	}
	
	@GET
	@Operation(
		summary = "Searches for images"
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
	public Response search(
		@BeanParam ImageSearch searchObject
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
	public ImageGet get(
		@PathParam("id") String id
	) {
		return super.get(id);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular image, adds a new revision.",
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
	public ImageGet updateObj(
		@PathParam("id")
		String id,
		ObjectNode updates
	) {
		return super.updateObj(id, updates);
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
	public Response getRevisionData(
		@PathParam("id")
		String id,
		@PathParam("rev")
		String revision
	) throws IOException {
		return super.getRevisionData(id, revision);
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
	@Override
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		return super.searchHistory(searchObject);
	}
	
	
	private Response getImageFromObject(MongoObjectService<? extends ImagedMainObject, ?, ?> service, String id) throws IOException {
		String objTypeName = service.getClazz().getSimpleName();
		log.info("Retrieving image for {} of id \"{}\"", objTypeName, id);
		
		ImagedMainObject object = service.get(this.getOqmDbIdOrName(), id);
		
		if (object == null) {
			log.info("{} not found.", objTypeName);
			return Response.status(Response.Status.NOT_FOUND)
					   .type(MediaType.TEXT_PLAIN_TYPE)
					   .entity(objTypeName + " not found.").build();
		}
		
		if (object.getImageIds().isEmpty()) {
			log.info("Storage block has no images. Returning blank placeholder image.");
			return Response
					   .seeOther(EMPTY_IMAGE_URI)
					   .build();
		}
		
		ObjectId imageId = object.getImageIds().get(0);
		
		return this.getRevisionData(imageId.toHexString(), "latest");
	}
	
	@GET
	@Path("for/{object}/{id}")
	@Operation(
		summary = "Gets the image data for the first image held of an imaged object."
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces({
		"image/png",
		"text/plain"
	})
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response getImageDataForObject(
		@PathParam("object") IMAGED_OBJ_TYPE_NAME object,
		@PathParam("id") String id
	) throws IOException {
		log.info("Retrieving image for {} of id \"{}\"", object, id);
		
		switch (object) {
			case storageBlock -> {
				return this.getImageFromObject(this.storageBlockService, id);
			}
			case item -> {
				return this.getImageFromObject(this.itemService, id);
			}
			case item_category -> {
				return this.getImageFromObject(this.itemCategoryService, id);
			}
			case item_stored -> {
				return this.getImageFromObject(this.itemStoredService, id);
			}
			default -> {
				log.error("Should not have gotten to this point... server error.");
				return Response.status(Response.Status.NOT_FOUND)
						   .type(MediaType.TEXT_PLAIN_TYPE)
						   .entity("No imaged object of type \"" + object + "\"")
						   .build();
			}
		}
	}
}
