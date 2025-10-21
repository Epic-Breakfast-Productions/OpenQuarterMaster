package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.storage;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.tree.storageBlock.StorageBlockTree;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.StorageBlockSearch;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;

import java.util.List;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1_DB_AWARE + "/inventory/storage-block")
@Tags({@Tag(name = "Storage Blocks", description = "Endpoints for managing Storage Blocks.")})
@RequestScoped
public class StorageCrud extends MainObjectProvider<StorageBlock, StorageBlockSearch> {
	
	@Inject
	@Getter
	StorageBlockService objectService;
	
	@Getter
	Class<StorageBlock> objectClass =  StorageBlock.class;
	
	@POST
	@Operation(
		summary = "Adds a new Storage Block."
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
	@Override
	public StorageBlock create(
		@Valid StorageBlock storageBlock
	) {
		return super.create(storageBlock);
	}
	
	@POST
	@Path("bulk")
	@Operation(
		summary = "Adds new Storage Blocks."
	)
	@APIResponse(
		responseCode = "200",
		description = "Objects added.",
		content = @Content(
			mediaType = "application/json",
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<ObjectId> createBulk(
		@Valid List<StorageBlock> storageBlocks
	) {
		return super.createBulk(storageBlocks);
	}
	
	@GET
	@Operation(
		summary = "Gets a list of storage blocks, using search parameters."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved."
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Override
	public SearchResult<StorageBlock> search(
		@BeanParam StorageBlockSearch blockSearch
	) {
		return super.search(blockSearch);
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
				implementation = CollectionStats.class
			)
		)
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public CollectionStats getCollectionStats(
	) {
		return super.getCollectionStats();
	}
	
	@Path("{id}")
	@GET
	@Operation(
		summary = "Gets a particular Storage Block."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = StorageBlock.class
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
	@Override
	public StorageBlock get(
		@PathParam("id") String id
	) {
		return super.get(id);
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a storage block.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Storage block updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = StorageBlock.class
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
	@Override
	public StorageBlock update(
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
				implementation = MainObject.class
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
	@Override
	public StorageBlock delete(
		@PathParam("id") String id
	) {
		return super.delete(id);
	}
	
	@GET
	@Path("tree")
	@Operation(
		summary = "Gets a tree of the storage blocks."
	)
	@APIResponse(
		responseCode = "200",
		description = "Tree retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					implementation = StorageBlockTree.class
				)
			)
		}
	)
	@APIResponse(
		responseCode = "204",
		description = "No items found from query given.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces({MediaType.APPLICATION_JSON})
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public StorageBlockTree tree(
		//for actual queries
		@QueryParam("onlyInclude") List<ObjectId> onlyInclude
	) {
		return (StorageBlockTree) ((StorageBlockService) this.getObjectService()).getTree(this.getOqmDbIdOrName(), onlyInclude);
	}
	
	
	//<editor-fold desc="History">
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
		}
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		return super.searchHistory(searchObject);
	}
	
	//</editor-fold>
	
//	@GET
//	@Path("{id}/children")
//	@Operation(
//		summary = "Gets children of a particular storage block."
//	)
//	@APIResponse(
//		responseCode = "200",
//		description = "Blocks retrieved.",
//		content = {
//			@Content(
//				mediaType = "application/json",
//				schema = @Schema(
//					type = SchemaType.ARRAY,
//					implementation = StorageBlock.class
//				)
//			)
//		},
//		headers = {
//			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
//			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
//		}
//	)
//	@Produces({MediaType.APPLICATION_JSON})
//	@RolesAllowed(Roles.INVENTORY_VIEW)
//	public Response getChildrenOfBlock(
//		@PathParam("id") String storageBlockId
//	) {
//		log.info("Getting children of \"{}\"", storageBlockId);
//		return Response.ok(((StorageBlockService)this.getObjectService()).getChildrenIn(storageBlockId)).build();
//	}
}
