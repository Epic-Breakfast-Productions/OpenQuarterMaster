package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.media;

import com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.EndpointProvider;
import com.ebp.openQuarterMaster.baseStation.service.mongo.ImageService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.MongoService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingCalculations;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SortType;
import com.ebp.openQuarterMaster.lib.core.ImagedMainObject;
import com.ebp.openQuarterMaster.lib.core.media.Image;
import com.ebp.openQuarterMaster.lib.core.rest.media.ImageCreateRequest;
import com.ebp.openQuarterMaster.lib.core.rest.storage.IMAGED_OBJ_TYPE_NAME;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
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

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

@Traced
@Slf4j
@Path("/api/media/image")
@Tags({@Tag(name = "Media", description = "Endpoints for media CRUD")})
@RequestScoped
public class ImageCrud extends EndpointProvider {
	
	private static final URI EMPTY_IMAGE_URI;
	
	static {
		try {
			EMPTY_IMAGE_URI = new URI("/media/empty.svg");
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Inject
	ImageService imageService;
	@Inject
	UserService userService;
	@Inject
	StorageBlockService storageBlockService;
	@Inject
	InventoryItemService itemService;
	@Inject
	JsonWebToken jwt;
	@Inject
	Validator validator;
	@Inject
	@Location("tags/search/image/imageSearchResults.html")
	Template imageSearchResultsTemplate;
	
	@POST
	@Operation(
		summary = "Adds a new image."
	)
	@APIResponse(
		responseCode = "201",
		description = "Storage Block added.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ObjectId.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.)",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed("user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createImage(
		@Context SecurityContext securityContext,
		@Valid ImageCreateRequest icr
	) throws IOException {
		logRequestContext(this.jwt, securityContext);
		log.info("Creating new image.");
		User user = this.userService.getFromJwt(jwt);
		
		Image image = new Image(icr);
		
		this.validator.validate(image);
		
		ObjectId output = imageService.add(image, user);
		log.info("Image created with id: {}", output);
		return Response.status(Response.Status.CREATED).entity(output).build();
	}
	
	@GET
	@Operation(
		summary = "Gets a list of images."
	)
	@APIResponse(
		responseCode = "200",
		description = "Images retrieved.",
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
	@APIResponse(
		responseCode = "204",
		description = "No items found from query given.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed("user")
	public Response listImages(
		@Context SecurityContext securityContext,
		//for actual queries
		@QueryParam("title") String imageTitle,
		//attKeywords
		@QueryParam("keyword") List<String> keywords,
		@QueryParam("attributeKey") List<String> attributeKeys,
		@QueryParam("attributeValue") List<String> attributeValues,
		//paging
		@QueryParam("pageSize") Integer pageSize,
		@QueryParam("pageNum") Integer pageNum,
		//sorting
		@QueryParam("sortBy") String sortField,
		@QueryParam("sortType") SortType sortType,
		//options for html rendering
		@HeaderParam("accept") String acceptHeaderVal,
		@HeaderParam("actionType") String actionTypeHeaderVal,
		@HeaderParam("searchFormId") String searchFormIdHeaderVal,
		@HeaderParam("inputIdPrepend") String inputIdPrependHeaderVal,
		@HeaderParam("otherModalId") String otherModalIdHeaderVal
	) {
		logRequestContext(this.jwt, securityContext);
		log.info("Searching for storage blocks with: ");
		
		Bson sort = SearchUtils.getSortBson(sortField, sortType);
		PagingOptions pageOptions = PagingOptions.fromQueryParams(pageSize, pageNum, (!MediaType.TEXT_HTML.equals(acceptHeaderVal)));
		
		SearchResult<Image> output = this.imageService.search(
			imageTitle,
			keywords,
			SearchUtils.attListsToMap(attributeKeys, attributeValues),
			sort,
			pageOptions
		);
		
		if (output.getResults().isEmpty()) {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
		
		Response.ResponseBuilder rb = Response
			.status(Response.Status.OK)
			.header("num-elements", output.getResults().size())
			.header("query-num-results", output.getNumResultsForEntireQuery());
		log.debug("Accept header value: \"{}\"", acceptHeaderVal);
		switch (acceptHeaderVal) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				rb = rb.entity(
						   this.imageSearchResultsTemplate
							   .data("searchResults", output)
							   .data("actionType", (actionTypeHeaderVal == null || acceptHeaderVal.isBlank() ? "full" : actionTypeHeaderVal))
							   .data(
								   "searchFormId",
								   (searchFormIdHeaderVal == null || searchFormIdHeaderVal.isBlank() ? "" : searchFormIdHeaderVal)
							   )
							   .data(
								   "inputIdPrepend",
								   (inputIdPrependHeaderVal == null || inputIdPrependHeaderVal.isBlank() ? "" : inputIdPrependHeaderVal)
							   )
							   .data(
								   "otherModalId",
								   (otherModalIdHeaderVal == null || otherModalIdHeaderVal.isBlank() ? "" : otherModalIdHeaderVal)
							   )
							   .data("pagingCalculations", new PagingCalculations(pageOptions, output))
						   //                                        .data("storageService", this.storageBlockService)
					   )
					   .type(MediaType.TEXT_HTML_TYPE);
				break;
			case MediaType.APPLICATION_JSON:
			default:
				log.debug("Requestor wanted json, or any other form");
				rb = rb.entity(output.getResults())
					   .type(MediaType.APPLICATION_JSON_TYPE);
		}
		
		return rb.build();
	}
	
	@GET
	@Path("{id}")
	@Operation(
		summary = "Gets a particular image."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
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
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("user")
	public Response getImage(
		@Context SecurityContext securityContext,
		@org.jboss.resteasy.annotations.jaxrs.PathParam String id
	) {
		logRequestContext(this.jwt, securityContext);
		log.info("Retrieving image with id {}", id);
		Image output = this.imageService.get(id);
		
		if (output == null) {
			log.info("Image not found.");
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		log.info("Image found");
		return Response.status(Response.Status.OK).entity(output).build();
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
		logRequestContext(this.jwt, securityContext);
		log.info("Retrieving image with id \"{}\"'s data", id);
		Image output = this.imageService.get(id);
		
		if (output == null) {
			log.info("Image not found.");
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		log.info("Image found");
		return Response.status(Response.Status.OK)
					   .entity(Base64.getDecoder().decode(output.getData()))
					   .type("image/" + output.getType())
					   .build();
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular image.",
		description = "Partial update to an image. Do not need to supply all fields, just the one you wish to update."
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
	@RolesAllowed("user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateImage(
		@Context SecurityContext securityContext,
		@org.jboss.resteasy.annotations.jaxrs.PathParam String id,
		ObjectNode imageUpdates
	) {
		logRequestContext(this.jwt, securityContext);
		log.info("Updating image with id {}", id);
		User user = this.userService.getFromJwt(jwt);
		
		Image updated = this.imageService.update(id, imageUpdates, user);
		
		return Response.ok(updated).build();
	}
	
	@DELETE
	@Path("{id}")
	@Operation(
		summary = "Deletes a particular Image."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image deleted.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = Image.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "No Image found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed("user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteImage(
		@Context SecurityContext securityContext,
		@org.jboss.resteasy.annotations.jaxrs.PathParam String id
	) {
		logRequestContext(this.jwt, securityContext);
		log.info("Deleting image with id {}", id);
		User user = this.userService.getFromJwt(jwt);
		Image output = imageService.remove(id, user);
		
		if (output == null) {
			log.info("Image not found.");
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		log.info("Image found, deleted.");
		return Response.status(Response.Status.OK).entity(output).build();
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
		
		Image output = this.imageService.get(imageId);
		
		if (output == null) {
			log.info("Image not found.");
			return Response
				.status(Response.Status.NOT_FOUND)
				.type(MediaType.TEXT_PLAIN_TYPE)
				.entity("Image not found.").build();
		}
		
		log.info("Image found ({}) {}", output.getType(), output.getId());
		return Response.status(Response.Status.OK)
					   .entity(Base64.getDecoder().decode(output.getData()))
					   .type(output.getMimeType())
					   .build();
	}
	
	@GET
	@Path("for/{object}/{id}")
	@Operation(
		summary = "Gets the image data for the first image held of a storage block."
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
		logRequestContext(this.jwt, securityContext);
		log.info("Retrieving image for storage block of id \"{}\"", id);
		
		switch (object){
			case storageBlock:
				return this.getImageFromObject(this.storageBlockService, id);
			case item:
				return this.getImageFromObject(this.itemService, id);
		}
		log.error("Should not have gotten to this point... server error.");
		return Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN_TYPE).entity("No imaged object of type \"" + object + "\"").build();
	}
}
