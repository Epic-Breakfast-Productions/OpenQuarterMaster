package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
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
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.baseStation.rest.dataImportExport.ImportBundleFileBody;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.InventoryItemSearch;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.lib.core.Utils;
import tech.ebp.oqm.lib.core.object.history.ObjectHistory;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.user.User;
import tech.ebp.oqm.lib.core.rest.ErrorMessage;
import tech.ebp.oqm.lib.core.rest.auth.roles.Roles;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
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
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

@Traced
@Slf4j
@Path("/api/inventory/item")
@Tags({@Tag(name = "Inventory Items", description = "Endpoints for inventory item CRUD, and managing stored items.")})
@RequestScoped
public class InventoryItemsCrud extends MainObjectProvider<InventoryItem, InventoryItemSearch> {
	
	
	Template itemSearchResultsTemplate;
	ObjectMapper objectMapper;
	
	@Inject
	public InventoryItemsCrud(
		InventoryItemService inventoryItemService,
		UserService userService,
		JsonWebToken jwt,
		@Location("tags/objView/objHistoryViewRows.html")
		Template historyRowsTemplate,
		@Location("tags/search/item/itemSearchResults.html")
		Template itemSearchResultsTemplate,
		ObjectMapper objectMapper
	) {
		super(InventoryItem.class, inventoryItemService, userService, jwt, historyRowsTemplate);
		this.itemSearchResultsTemplate = itemSearchResultsTemplate;
		this.objectMapper = objectMapper;
	}
	
	@POST
	@Operation(
		summary = "Adds a new inventory item."
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
		@Context SecurityContext securityContext,
		@Valid InventoryItem item
	) {
		return super.create(securityContext, item);
	}
	
	@POST
	@Operation(
		summary = "Imports items from a file uploaded by a user."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
			schema = @Schema(
				type = SchemaType.ARRAY,
				implementation = ObjectId.class
			)
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response importData(
		@Context SecurityContext securityContext,
		@MultipartForm ImportBundleFileBody body
	) throws IOException {
		logRequestContext(this.getJwt(), securityContext);
		User user = this.getUserFromJwt();
		
		log.info("Processing item file: {}", body.fileName);
		
		final String fileExtension = FilenameUtils.getExtension(body.fileName);
		
		List<InventoryItem<?, ?, ?>> items = new ArrayList<>();
		switch (fileExtension) {
			case "csv":
				return Response.status(Response.Status.NOT_IMPLEMENTED).entity(
					ErrorMessage.builder()
								.displayMessage("Adding items from CSV not yet implemented.")
								.build()
				).build();
			case "json":
				JsonNode json = this.objectMapper.readTree(body.file);
				
				if (json.isObject()) {
					json = this.objectMapper.createArrayNode().add(json);
				}
				
				while (!(json).isEmpty()) {
					JsonNode curItemJson = ((ArrayNode) json).remove(0);
					items.add(this.objectMapper.treeToValue(curItemJson, InventoryItem.class));
				}
				
				break;
			default:
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid file type uploaded.").build();
		}
		
		List<ObjectId> results = new ArrayList<>(items.size());
		try (ClientSession session = this.getObjectService().getNewClientSession()) {
			session.startTransaction();
			while (!items.isEmpty()) {
				results.add(
					this.getObjectService().add(
						session,
						items.remove(0),
						user
					)
				);
			}
			session.commitTransaction();
		}
		
		return Response.ok(results).build();
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
					implementation = InventoryItem.class
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
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response search(
		@Context SecurityContext securityContext,
		//for actual queries
		@BeanParam InventoryItemSearch itemSearch
	) {
		Tuple2<Response.ResponseBuilder, SearchResult<InventoryItem>> tuple = super.getSearchResponseBuilder(securityContext, itemSearch);
		Response.ResponseBuilder rb = tuple.getItem1();
		
		log.debug("Accept header value: \"{}\"", itemSearch.getAcceptHeaderVal());
		switch (itemSearch.getAcceptHeaderVal()) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				SearchResult<InventoryItem> output = tuple.getItem2();
				rb = rb.entity(
						   this.itemSearchResultsTemplate
							   .data("searchResults", output)
							   .data("actionType", (
								   itemSearch.getActionTypeHeaderVal() == null || itemSearch.getActionTypeHeaderVal().isBlank() ? "full" :
									   itemSearch.getActionTypeHeaderVal()
							   ))
							   .data(
								   "searchFormId",
								   (
									   itemSearch.getSearchFormIdHeaderVal() == null || itemSearch.getSearchFormIdHeaderVal().isBlank() ?
										   "" :
										   itemSearch.getSearchFormIdHeaderVal()
								   )
							   )
							   .data(
								   "inputIdPrepend",
								   (
									   itemSearch.getInputIdPrependHeaderVal() == null || itemSearch.getInputIdPrependHeaderVal().isBlank() ?
										   "" :
										   itemSearch.getInputIdPrependHeaderVal()
								   )
							   )
							   .data(
								   "otherModalId",
								   (
									   itemSearch.getOtherModalIdHeaderVal() == null || itemSearch.getOtherModalIdHeaderVal().isBlank() ?
										   "" :
										   itemSearch.getOtherModalIdHeaderVal()
								   )
							   )
							   .data("pagingCalculations", new PagingCalculations(output))
							   .data("storageService", this.getObjectService())
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
		summary = "Gets a particular InventoryItem."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
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
	public InventoryItem get(
		@Context SecurityContext securityContext,
		@PathParam String id
	) {
		return super.get(securityContext, id);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular Object.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
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
	public InventoryItem update(
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
				implementation = InventoryItem.class
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
	public InventoryItem delete(
		@Context SecurityContext securityContext,
		@PathParam String id
	) {
		return super.delete(securityContext, id);
	}
	
	@GET
	@Path("{id}/history")
	@Operation(
		summary = "Gets a particular Inventory Item's history."
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
	@RolesAllowed(Roles.INVENTORY_VIEW)
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
		summary = "Searches the history for the inventory items."
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
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<ObjectHistory> searchHistory(
		@Context SecurityContext securityContext,
		@BeanParam HistorySearch searchObject
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Searching for objects with: {}", searchObject);
		
		return this.getObjectService().searchHistory(searchObject, false);
	}
	
	@GET
	@Path("{itemId}/{storageBlockId}")
	@Operation(
		summary = "Gets the stored amount or tracked item to the storage block specified."
	)
	@APIResponse(
		responseCode = "200",
		description = "Item added.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "No item found to get.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStoredInventoryItem(
		@Context SecurityContext securityContext,
		@PathParam String itemId,
		@PathParam String storageBlockId
	) {
		logRequestContext(this.getJwt(), securityContext);
		//TODO
		return Response.serverError().entity("Not implemented yet.").build();
	}
	
	
	@PUT
	@Path("{itemId}/{storageBlockId}")
	@Operation(
		summary = "Adds a stored amount or tracked item to the storage block specified."
	)
	@APIResponse(
		responseCode = "200",
		description = "Item added to.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "No item found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	public Response addStoredInventoryItem(
		@Context SecurityContext securityContext,
		@PathParam String itemId,
		@PathParam String storageBlockId,
		JsonNode addObject
	) throws JsonProcessingException {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Adding to item");
		InventoryItem item = this.getObjectService().get(itemId);
		
		item = ((InventoryItemService) this.getObjectService()).add(
			itemId,
			storageBlockId,
			(Stored) Utils.OBJECT_MAPPER.treeToValue(
				addObject,
				((Class) ((ParameterizedType) item.getClass().getGenericSuperclass()).getActualTypeArguments()[0])
			)
		);
		
		return Response.ok(item).build();
	}
	
	@DELETE
	@Path("{itemId}/{storageBlockId}")
	@Operation(
		summary = "Subtracts a stored amount or tracked item from the storage block specified."
	)
	@APIResponse(
		responseCode = "200",
		description = "Item subtracted from.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "No item found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	public Response subtractStoredInventoryItem(
		@Context SecurityContext securityContext,
		@PathParam String itemId,
		@PathParam String storageBlockId,
		JsonNode subtractObject
	) throws JsonProcessingException {
		logRequestContext(this.getJwt(), securityContext);
		
		InventoryItem item = this.getObjectService().get(itemId);
		
		item = ((InventoryItemService) this.getObjectService()).subtract(
			itemId,
			storageBlockId,
			(Stored) Utils.OBJECT_MAPPER.treeToValue(
				subtractObject,
				((Class) ((ParameterizedType) item.getClass().getGenericSuperclass()).getActualTypeArguments()[0])
			)
		);
		
		return Response.ok(item).build();
	}
	
	@PUT
	@Path("{itemId}/{storageBlockIdFrom}/{storageBlockIdTo}")
	@Operation(
		summary = "Transfers a stored amount or tracked item to the storage block specified."
	)
	@APIResponse(
		responseCode = "200",
		description = "Item added.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "No item found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_EDIT)
	public Response transferStoredInventoryItem(
		@Context SecurityContext securityContext,
		@PathParam String itemId,
		@PathParam String storageBlockIdFrom,
		@PathParam String storageBlockIdTo,
		JsonNode transferObject
	) throws JsonProcessingException {
		logRequestContext(this.getJwt(), securityContext);
		
		InventoryItem item = this.getObjectService().get(itemId);
		
		item = ((InventoryItemService) this.getObjectService()).transfer(
			itemId,
			storageBlockIdFrom,
			storageBlockIdTo,
			(Stored) Utils.OBJECT_MAPPER.treeToValue(
				transferObject,
				((Class) ((ParameterizedType) item.getClass().getGenericSuperclass()).getActualTypeArguments()[0])
			)
		);
		
		return Response.ok(item).build();
	}
	
	@GET
	@Path("inStorageBlock/{storageBlockId}")
	@Operation(
		summary = "Gets items that ."
	)
	@APIResponse(
		responseCode = "200",
		description = "Item added.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				type = SchemaType.ARRAY,
				implementation = InventoryItem.class
			)
		)
	)
	@APIResponse(
		responseCode = "404",
		description = "No item found to get.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInventoryItemsInBlock(
		@Context SecurityContext securityContext,
		@PathParam("storageBlockId") String storageBlockId
	) {
		logRequestContext(this.getJwt(), securityContext);
		
		return Response.ok(((InventoryItemService) this.getObjectService()).getItemsInBlock(storageBlockId)).build();
	}
	
}
