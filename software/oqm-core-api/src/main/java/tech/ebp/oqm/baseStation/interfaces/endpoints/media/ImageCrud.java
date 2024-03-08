package tech.ebp.oqm.baseStation.interfaces.endpoints.media;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainFileObjectProvider;
import tech.ebp.oqm.baseStation.model.object.ImagedMainObject;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.media.Image;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.media.ImageGet;
import tech.ebp.oqm.baseStation.model.rest.storage.IMAGED_OBJ_TYPE_NAME;
import tech.ebp.oqm.baseStation.rest.file.FileUploadBody;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.ImageSearch;
import tech.ebp.oqm.baseStation.service.mongo.image.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.ItemCategoryService;
import tech.ebp.oqm.baseStation.service.mongo.MongoObjectService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

//@Slf4j
//@Path(ROOT_API_ENDPOINT_V1 + "/media/image")
//@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
//@RequestScoped
//public class ImageCrud extends MainFileObjectProvider<Image, FileUploadBody, ImageSearch, ImageGet> {
//
//	private static final URI EMPTY_IMAGE_URI;
//
//	static {
//		try {
//			EMPTY_IMAGE_URI = new URI("/media/empty.svg");
//		} catch(URISyntaxException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	@Inject
//	StorageBlockService storageBlockService;
//	@Inject
//	InventoryItemService itemService;
//	@Inject
//	ItemCategoryService itemCategoryService;
//	@Inject
//	Validator validator;
//
//	@Inject
//	@Getter
//	ImageService fileService;
//
//	@Getter
//	Class<Image> objectClass =  Image.class;
	
	//<editor-fold desc="CRUD operations">
	//TODO:: add CRUD
	
//	private Response getImageFromObject(MongoObjectService<? extends ImagedMainObject, ?, ?> service, String id) {
//		String objTypeName = service.getClazz().getSimpleName();
//		log.info("Retrieving image for {} of id \"{}\"", objTypeName, id);
//
//		ImagedMainObject object = service.get(id);
//
//		if (object == null) {
//			log.info("{} not found.", objTypeName);
//			return Response.status(Response.Status.NOT_FOUND)
//					   .type(MediaType.TEXT_PLAIN_TYPE)
//					   .entity(objTypeName + " not found.").build();
//		}
//
//		if (object.getImageIds().isEmpty()) {
//			log.info("Storage block has no images. Returning blank placeholder image.");
//			return Response
//					   .seeOther(EMPTY_IMAGE_URI)
//					   .build();
//		}
//
//		ObjectId imageId = object.getImageIds().get(0);
//
//		Image output = this.getFileService().get(imageId);
//
//		log.info("Image found ({}) {}", output.getType(), output.getId());
//		return Response.status(Response.Status.OK)
//				   .entity(Base64.getDecoder().decode(output.getData()))
//				   .type(output.getMimeType())
//				   .build();
//	}
//
//	@GET
//	@Path("for/{object}/{id}")
//	@Operation(
//		summary = "Gets the image data for the first image held of an imaged object."
//	)
//	@APIResponse(
//		responseCode = "400",
//		description = "Bad request given. Data given could not pass validation.",
//		content = @Content(mediaType = "text/plain")
//	)
//	@Produces({
//		"image/png",
//		"text/plain"
//	})
//	@RolesAllowed(Roles.INVENTORY_VIEW)
//	public Response getImageDataForObject(
//		@PathParam("object") IMAGED_OBJ_TYPE_NAME object,
//		@PathParam("id") String id
//	) {
//		log.info("Retrieving image for {} of id \"{}\"", object, id);
//
//		switch (object) {
//			case storageBlock -> {
//				return this.getImageFromObject(this.storageBlockService, id);
//			}
//			case item -> {
//				return this.getImageFromObject(this.itemService, id);
//			}
//			case item_category -> {
//				return this.getImageFromObject(this.itemCategoryService, id);
//			}
//			default -> {
//				log.error("Should not have gotten to this point... server error.");
//				return Response.status(Response.Status.NOT_FOUND)
//						   .type(MediaType.TEXT_PLAIN_TYPE)
//						   .entity("No imaged object of type \"" + object + "\"")
//						   .build();
//			}
//		}
//	}
//}
