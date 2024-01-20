package tech.ebp.oqm.baseStation.interfaces.endpoints.media;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
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
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.baseStation.model.object.ImagedMainObject;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.media.Image;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.media.ImageCreateRequest;
import tech.ebp.oqm.baseStation.model.rest.storage.IMAGED_OBJ_TYPE_NAME;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.ImageSearch;
import tech.ebp.oqm.baseStation.service.mongo.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.ItemCategoryService;
import tech.ebp.oqm.baseStation.service.mongo.MongoObjectService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/media/image")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@RequestScoped
public class ImageCrud extends MainObjectProvider<Image, ImageSearch> {
	
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
	Validator validator;
	
	@Inject
	@Getter
	ImageService objectService;
	
	@Getter
	Class<Image> objectClass =  Image.class;
	
	//<editor-fold desc="CRUD operations">
	
	@POST
	@Operation(
		summary = "Adds a new image."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = "application/json",
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectId create(
		@Valid ImageCreateRequest icr
	) {
		Image image = new Image(icr);
		
		this.validator.validate(image);
		return super.create(image);
	}
	
	@GET
	@Operation(
		summary = "Gets a list of objects, using search parameters."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					type = SchemaType.ARRAY,
					implementation = Image.class
				)
			)
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response search(
		@BeanParam ImageSearch searchObject
	) {
		return super.search(searchObject);
	}
	
	@Path("{id}")
	@GET
	@Operation(
		summary = "Gets a particular object."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = Image.class
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
	public Image get(
		@PathParam("id") String id
	) {
		return super.get(id);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular Image.",
		description = "Partial update to an image. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = Image.class
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
	@Produces(MediaType.APPLICATION_JSON)
	public Image update(
		@PathParam("id") String id,
		ObjectNode updates
	) {
		//TODO:: handle updates, json given is icr
		return super.update(id, updates);
	}
	
	@DELETE
	@Path("{id}")
	@Operation(
		summary = "Deletes a particular object."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object deleted.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = Image.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "Bad request given, could not find object at given id.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "410",
		description = "Object requested has already been deleted.",
		content = @Content(mediaType = "text/plain")
	)
	@APIResponse(
		responseCode = "404",
		description = "No object found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Produces(MediaType.APPLICATION_JSON)
	public Image delete(
		@PathParam("id") String id
	) {
		return super.delete(id);
	}
	
	@GET
	@Path("{id}/history")
	@Operation(
		summary = "Gets a particular object's history."
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
	public Response getHistoryForObject(
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject
	) {
		return super.getHistoryForObject(id, searchObject);
	}
	
	@GET
	@Path("history")
	@Operation(
		summary = "Searches the history for the images."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					type = SchemaType.ARRAY,
					implementation = ObjectHistoryEvent.class
				)
			)
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		return super.searchHistory(searchObject);
	}
	
	@GET
	@Path("{id}/data")
	@Operation(
		summary = "Gets a particular image's data string for use in html images."
	)
	//    @APIResponse(
	//            responseCode = "200",
	//            description = "Image retrieved."
	////            content = @Content( //TODO
	//            )
	//    )
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	//    @Produces(MediaType.)//TODO
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response getImageData(
		@PathParam("id") String id
	) {
		log.info("Retrieving image with id \"{}\"'s data", id);
		Image output = this.getObjectService().get(id);
		
		log.info("Image found.");
		return Response.status(Response.Status.OK)
				   .entity(Base64.getDecoder().decode(output.getData()))
				   .type("image/" + output.getType())
				   .build();
	}
	
	private Response getImageFromObject(MongoObjectService<? extends ImagedMainObject, ?> service, String id) {
		String objTypeName = service.getClazz().getSimpleName();
		log.info("Retrieving image for {} of id \"{}\"", objTypeName, id);
		
		ImagedMainObject object = service.get(id);
		
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
		
		Image output = this.getObjectService().get(imageId);
		
		log.info("Image found ({}) {}", output.getType(), output.getId());
		return Response.status(Response.Status.OK)
				   .entity(Base64.getDecoder().decode(output.getData()))
				   .type(output.getMimeType())
				   .build();
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
	) {
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
