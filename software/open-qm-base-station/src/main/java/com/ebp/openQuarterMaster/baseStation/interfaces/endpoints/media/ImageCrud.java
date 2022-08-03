package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.media;

import com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.MainObjectProvider;
import com.ebp.openQuarterMaster.baseStation.rest.search.HistorySearch;
import com.ebp.openQuarterMaster.baseStation.rest.search.ImageSearch;
import com.ebp.openQuarterMaster.baseStation.service.mongo.ImageService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.MongoService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingCalculations;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.lib.core.ImagedMainObject;
import com.ebp.openQuarterMaster.lib.core.history.ObjectHistory;
import com.ebp.openQuarterMaster.lib.core.media.Image;
import com.ebp.openQuarterMaster.lib.core.rest.media.ImageCreateRequest;
import com.ebp.openQuarterMaster.lib.core.rest.storage.IMAGED_OBJ_TYPE_NAME;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

@Traced
@Slf4j
@Path("/api/media/image")
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
	
	StorageBlockService storageBlockService;
	InventoryItemService itemService;
	Template imageSearchResultsTemplate;
	Validator validator;
	
	@Inject
	public ImageCrud(
		ImageService imageService,
		UserService userService,
		JsonWebToken jwt,
		@Location("tags/objView/objHistoryViewRows.html")
		Template historyRowsTemplate,
		StorageBlockService storageBlockService,
		InventoryItemService itemService,
		@Location("tags/search/image/imageSearchResults.html")
		Template imageSearchResultsTemplate,
		Validator validator
	) {
		super(Image.class, imageService, userService, jwt, historyRowsTemplate);
		this.storageBlockService = storageBlockService;
		this.itemService = itemService;
		this.validator = validator;
		this.imageSearchResultsTemplate = imageSearchResultsTemplate;
	}
	
	
	//<editor-fold desc="CRUD operations">
	
	@POST
	@Operation(
		summary = "Adds a new object."
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
	@RolesAllowed("user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectId create(
		@Context SecurityContext securityContext,
		@Valid ImageCreateRequest icr
	) {
		Image image = new Image(icr);
		
		this.validator.validate(image);
		return super.create(securityContext, image);
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
			),
			@Content(
				mediaType = "text/html",
				schema = @Schema(type = SchemaType.STRING)
			)
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed("user")
	public Response search(
		@Context SecurityContext securityContext,
		@BeanParam ImageSearch searchObject
	) {
		Tuple2<Response.ResponseBuilder, SearchResult<Image>> tuple = super.getSearchResponseBuilder(securityContext, searchObject);
		Response.ResponseBuilder rb = tuple.getItem1();
		
		log.debug("Accept header value: \"{}\"", searchObject.getAcceptHeaderVal());
		switch (searchObject.getAcceptHeaderVal()) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				SearchResult<Image> output = tuple.getItem2();
				rb = rb.entity(
						   this.imageSearchResultsTemplate
							   .data("searchResults", output)
							   .data(
								   "actionType",
								   (
									   searchObject.getActionTypeHeaderVal() == null || searchObject.getAcceptHeaderVal().isBlank() ?
										   "full" :
										   searchObject.getActionTypeHeaderVal()
								   )
							   )
							   .data(
								   "searchFormId",
								   (
									   searchObject.getSearchFormIdHeaderVal() == null || searchObject.getSearchFormIdHeaderVal().isBlank() ? "" :
										   searchObject.getSearchFormIdHeaderVal()
								   )
							   )
							   .data(
								   "inputIdPrepend",
								   (
									   searchObject.getInputIdPrependHeaderVal() == null || searchObject.getInputIdPrependHeaderVal().isBlank() ?
										   "" :
										   searchObject.getInputIdPrependHeaderVal()
								   )
							   )
							   .data(
								   "otherModalId",
								   (
									   searchObject.getOtherModalIdHeaderVal() == null || searchObject.getOtherModalIdHeaderVal().isBlank() ?
										   "" :
										   searchObject.getOtherModalIdHeaderVal()
								   )
							   )
							   .data("pagingCalculations", new PagingCalculations(searchObject.getPagingOptions(false), output))
						   //                                        .data("storageService", this.storageBlockService)
					   )
					   .type(MediaType.TEXT_HTML_TYPE);
				break;
			case MediaType.APPLICATION_JSON:
			default:
				log.debug("Requestor wanted json, or any other form");
		}
		
		return rb.build();
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
	@RolesAllowed("user")
	public Image get(
		@Context SecurityContext securityContext,
		@PathParam String id
	) {
		return super.get(securityContext, id);
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
	@RolesAllowed("user")
	@Produces(MediaType.APPLICATION_JSON)
	public Image update(
		@Context SecurityContext securityContext,
		@PathParam String id,
		ObjectNode updates
	) {
		return super.update(securityContext, id, updates);
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
	@RolesAllowed("user")
	@Produces(MediaType.APPLICATION_JSON)
	public Image delete(
		@Context SecurityContext securityContext,
		@PathParam String id
	) {
		return super.delete(securityContext, id);
	}
	
	@GET
	@Path("{id}/history")
	@Operation(
		summary = "Gets a particular Image's history."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ObjectHistory.class)
			),
			@Content(
				mediaType = "text/html",
				schema = @Schema(type = SchemaType.STRING)
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
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed("user")
	public Response getHistoryForObject(
		@Context SecurityContext securityContext,
		@PathParam String id,
		@HeaderParam("accept") String acceptHeaderVal
	) {
		return super.getHistoryForObject(securityContext, id, acceptHeaderVal);
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
					implementation = ObjectHistory.class
				)
			)
		},
		headers = {
			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
		}
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed("user")
	public SearchResult<ObjectHistory> searchHistory(
		@Context SecurityContext securityContext,
		@BeanParam HistorySearch searchObject
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Searching for objects with: {}", searchObject);
		
		return this.getObjectService().searchHistory(searchObject, false);
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
	@RolesAllowed("user")
	public Response getImageData(
		@Context SecurityContext securityContext,
		@org.jboss.resteasy.annotations.jaxrs.PathParam String id
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Retrieving image with id \"{}\"'s data", id);
		Image output = this.getObjectService().get(id);
		
		log.info("Image found.");
		return Response.status(Response.Status.OK)
					   .entity(Base64.getDecoder().decode(output.getData()))
					   .type("image/" + output.getType())
					   .build();
	}
	
	private Response getImageFromObject(MongoService<? extends ImagedMainObject, ?> service, String id) {
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
	@RolesAllowed("user")
	public Response getImageDataForStorageBlock(
		@Context SecurityContext securityContext,
		@org.jboss.resteasy.annotations.jaxrs.PathParam IMAGED_OBJ_TYPE_NAME object,
		@org.jboss.resteasy.annotations.jaxrs.PathParam String id
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Retrieving image for storage block of id \"{}\"", id);
		
		switch (object) {
			case storageBlock:
				return this.getImageFromObject(this.storageBlockService, id);
			case item:
				return this.getImageFromObject(this.itemService, id);
		}
		log.error("Should not have gotten to this point... server error.");
		return Response.status(Response.Status.NOT_FOUND)
					   .type(MediaType.TEXT_PLAIN_TYPE)
					   .entity("No imaged object of type \"" + object + "\"")
					   .build();
	}
}
