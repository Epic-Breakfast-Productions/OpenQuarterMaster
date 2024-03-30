package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
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
import tech.ebp.oqm.baseStation.model.collectionStats.CollectionStats;
import tech.ebp.oqm.baseStation.model.collectionStats.InvItemCollectionStats;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.storage.items.AddSubtractTransferAction;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.rest.dataImportExport.ImportBundleFileBody;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.InventoryItemSearch;
import tech.ebp.oqm.baseStation.service.importExport.csv.InvItemCsvConverter;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/inventory/item")
@Tags({@Tag(name = "Inventory Items", description = "Endpoints for inventory item CRUD, and managing stored items.")})
@RequestScoped
public class InventoryItemsCrud extends MainObjectProvider<InventoryItem, InventoryItemSearch> {
	
	@Inject
	ObjectMapper objectMapper;
	@Inject
	InvItemCsvConverter invItemCsvConverter;
	
	@Getter
	@Inject
	InventoryItemService objectService;
	
	@Getter
	Class<InventoryItem> objectClass = InventoryItem.class;
	
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
		@Valid InventoryItem item
	) {
		return super.create(item);
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
	@RolesAllowed(Roles.INVENTORY_EDIT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response importData(
		@BeanParam ImportBundleFileBody body
	) throws IOException {
		log.info("Processing item file: {}", body.fileName);
		
		final String fileExtension = FilenameUtils.getExtension(body.fileName);
		
		List<InventoryItem<?, ?, ?>> items = new ArrayList<>();
		switch (fileExtension) {
			case "csv":
				items.addAll(this.invItemCsvConverter.csvIsToItems(body.file));
				break;
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
						this.getInteractingEntity()
					)
				);
			}
			session.commitTransaction();
		}
		
		return Response.ok(results).build();
	}
	
	@Override
	@Path("stats")
	@GET
	@Operation(
		summary = "Gets stats on this object's collection."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = InvItemCollectionStats.class
			)
		)
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public InvItemCollectionStats getCollectionStats(
	) {
		return (InvItemCollectionStats) super.getCollectionStats();
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
		//for actual queries
		@BeanParam InventoryItemSearch itemSearch
	) {
		return super.search(itemSearch);
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
		@PathParam("id") String id
	) {
		return super.get(id);
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
		@PathParam("id") String id,
		ObjectNode updates
	) {
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
	@Path("{itemId}/stored/{storageBlockId}")
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
		@PathParam("itemId") String itemId,
		@PathParam("storageBlockId") String storageBlockId
	) {
		//TODO
		return Response.serverError().entity("Not implemented yet.").build();
	}
	
	@PUT
	@Path("{itemId}/stored/{storageBlockId}")
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
		@PathParam("itemId") String itemId,
		@PathParam("storageBlockId") String storageBlockId,
		JsonNode addObject
	) throws JsonProcessingException {
		log.info("Adding to item");
		InventoryItem item = this.getObjectService().get(itemId);
		
		item = ((InventoryItemService) this.getObjectService()).add(
			itemId,
			storageBlockId,
			(Stored) ObjectUtils.OBJECT_MAPPER.treeToValue(
				addObject,
				((Class) ((ParameterizedType) item.getClass().getGenericSuperclass()).getActualTypeArguments()[0])
			),
			this.getInteractingEntity()
		);
		
		return Response.ok(item).build();
	}
	
	@PUT
	@Path("{itemId}/stored/{storageBlockId}/{storedId}")
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
	public Response addStoredInventoryItemToStored(
		@PathParam("itemId") String itemId,
		@PathParam("storedId") String storedId,
		@PathParam("storageBlockId") String storageBlockId,
		JsonNode addObject
	) throws JsonProcessingException {
		log.info("Adding to item");
		InventoryItem item = this.getObjectService().get(itemId);
		
		item = ((InventoryItemService) this.getObjectService()).add(
			itemId,
			storageBlockId,
			storedId,
			(Stored) ObjectUtils.OBJECT_MAPPER.treeToValue(
				addObject,
				((Class) ((ParameterizedType) item.getClass().getGenericSuperclass()).getActualTypeArguments()[0])
			),
			this.getInteractingEntity()
		);
		
		return Response.ok(item).build();
	}
	
	@DELETE
	@Path("{itemId}/stored/{storageBlockId}")
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
		@PathParam("itemId") String itemId,
		@PathParam("storageBlockId") String storageBlockId,
		JsonNode subtractObject
	) throws JsonProcessingException {
		InventoryItem item = this.getObjectService().get(itemId);
		
		item = ((InventoryItemService) this.getObjectService()).subtract(
			itemId,
			storageBlockId,
			(Stored) ObjectUtils.OBJECT_MAPPER.treeToValue(
				subtractObject,
				((Class) ((ParameterizedType) item.getClass().getGenericSuperclass()).getActualTypeArguments()[0])
			),
			this.getInteractingEntity()
		);
		
		return Response.ok(item).build();
	}
	
	@DELETE
	@Path("{itemId}/stored/{storageBlockId}/{storedId}")
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
		@PathParam("itemId") String itemId,
		@PathParam("storedId") String storedId,
		@PathParam("storageBlockId") String storageBlockId,
		JsonNode subtractObject
	) throws JsonProcessingException {
		InventoryItem item = this.getObjectService().get(itemId);
		
		item = ((InventoryItemService) this.getObjectService()).subtract(
			itemId,
			storageBlockId,
			storedId,
			(Stored) ObjectUtils.OBJECT_MAPPER.treeToValue(
				subtractObject,
				((Class) ((ParameterizedType) item.getClass().getGenericSuperclass()).getActualTypeArguments()[0])
			),
			this.getInteractingEntity()
		);
		
		return Response.ok(item).build();
	}
	
	@PUT
	@Path("{itemId}/stored/{storageBlockIdFrom}/{storageBlockIdTo}")
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
		@PathParam("itemId") String itemId,
		@PathParam("storageBlockIdFrom") String storageBlockIdFrom,
		@PathParam("storageBlockIdTo") String storageBlockIdTo,
		JsonNode transferObject
	) throws JsonProcessingException {
		InventoryItem item = this.getObjectService().get(itemId);
		
		item = ((InventoryItemService) this.getObjectService()).transfer(
			itemId,
			storageBlockIdFrom,
			storageBlockIdTo,
			(Stored) ObjectUtils.OBJECT_MAPPER.treeToValue(
				transferObject,
				((Class) ((ParameterizedType) item.getClass().getGenericSuperclass()).getActualTypeArguments()[0])
			),
			this.getInteractingEntity()
		);
		
		return Response.ok(item).build();
	}
	
	@PUT
	@Path("{itemId}/stored/{storageBlockIdFrom}/{storedIdFrom}/{storageBlockIdTo}/{storedIdTo}")
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
		@PathParam("itemId") String itemId,
		@PathParam("storageBlockIdFrom") String storageBlockIdFrom,
		@PathParam("storedIdFrom") String storedIdFrom,
		@PathParam("storageBlockIdTo") String storageBlockIdTo,
		@PathParam("storedIdTo") String storedIdTo,
		JsonNode transferObject
	) throws JsonProcessingException {
		InventoryItem item = this.getObjectService().get(itemId);
		
		item = ((InventoryItemService) this.getObjectService()).transfer(
			itemId,
			storageBlockIdFrom,
			storedIdFrom,
			storageBlockIdTo,
			storedIdTo,
			(Stored) ObjectUtils.OBJECT_MAPPER.treeToValue(
				transferObject,
				((Class) ((ParameterizedType) item.getClass().getGenericSuperclass()).getActualTypeArguments()[0])
			),
			this.getInteractingEntity()
		);
		
		return Response.ok(item).build();
	}
	
	/**
	 * TODO:: add endpoint to support list of actions
	 *
	 * @param itemId
	 * @param action
	 *
	 * @return
	 * @throws JsonProcessingException
	 */
	@PUT
	@Path("{itemId}/stored/applyAddSubtractTransfer")
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
	public Response applyAddSubtractTransfer(
		@PathParam("itemId") String itemId,
		AddSubtractTransferAction action
	) throws JsonProcessingException {
		InventoryItem item = this.getObjectService().get(itemId);
		
		item = ((InventoryItemService) this.getObjectService()).apply(
			itemId,
			action,
			this.getInteractingEntity()
		);
		
		return Response.ok(item).build();
	}
	
	@GET
	@Path("inStorageBlock/{storageBlockId}")
	@Operation(
		summary = "Gets items that are stored in the given block."
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
		@PathParam("storageBlockId") String storageBlockId
	) {
		return Response.ok(((InventoryItemService) this.getObjectService()).getItemsInBlock(storageBlockId)).build();
	}
	
}
