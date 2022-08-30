package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory.storage;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.NoArgsConstructor;
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
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.StorageBlockSearch;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.utils.UserRoles;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.ObjectHistory;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.tree.StorageBlockTree;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Traced
@Slf4j
@Path("/api/inventory/storage-block")
@Tags({@Tag(name = "Storage Blocks", description = "Endpoints for managing Storage Blocks.")})
@RequestScoped
@NoArgsConstructor
public class StorageCrud extends MainObjectProvider<StorageBlock, StorageBlockSearch> {
	
	Template storageSearchResultsTemplate;
	
	@Inject
	public StorageCrud(
		StorageBlockService storageBlockService,
		UserService userService,
		JsonWebToken jwt,
		@Location("tags/objView/objHistoryViewRows.html")
		Template historyRowsTemplate,
		@Location("tags/search/storage/storageSearchResults.html")
		Template storageSearchResultsTemplate
	) {
		super(StorageBlock.class, storageBlockService, userService, jwt, historyRowsTemplate);
		this.storageSearchResultsTemplate = storageSearchResultsTemplate;
	}
	
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
	@RolesAllowed(UserRoles.INVENTORY_EDIT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectId create(
		@Context SecurityContext securityContext,
		@Valid StorageBlock storageBlock
	) {
		return super.create(securityContext, storageBlock);
	}
	
	@GET
	@Operation(
		summary = "Gets a list of storage blocks, using search parameters."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					type = SchemaType.ARRAY,
					implementation = StorageBlock.class
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
	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	@Override
	public Response search(
		@Context SecurityContext securityContext,
		@BeanParam StorageBlockSearch blockSearch
	) {
		Tuple2<Response.ResponseBuilder, SearchResult<StorageBlock>> tuple = super.getSearchResponseBuilder(securityContext, blockSearch);
		Response.ResponseBuilder rb = tuple.getItem1();
		
		log.debug("Accept header value: \"{}\"", blockSearch.getAcceptHeaderVal());
		switch (blockSearch.getAcceptHeaderVal()) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				SearchResult<StorageBlock> output = tuple.getItem2();
				rb = rb.entity(
						   this.storageSearchResultsTemplate
							   .data("searchResults", output)
							   .data("actionType", (
								   blockSearch.getActionTypeHeaderVal() == null || blockSearch.getActionTypeHeaderVal().isBlank() ? "full" :
									   blockSearch.getActionTypeHeaderVal()
							   ))
							   .data(
								   "searchFormId",
								   (
									   blockSearch.getSearchFormIdHeaderVal() == null || blockSearch.getSearchFormIdHeaderVal().isBlank() ?
										   "" :
										   blockSearch.getSearchFormIdHeaderVal()
								   )
							   )
							   .data(
								   "inputIdPrepend",
								   (
									   blockSearch.getInputIdPrependHeaderVal() == null || blockSearch.getInputIdPrependHeaderVal().isBlank() ?
										   "" :
										   blockSearch.getInputIdPrependHeaderVal()
								   )
							   )
							   .data(
								   "otherModalId",
								   (
									   blockSearch.getOtherModalIdHeaderVal() == null || blockSearch.getOtherModalIdHeaderVal().isBlank() ?
										   "" :
										   blockSearch.getOtherModalIdHeaderVal()
								   )
							   )
							   .data("pagingCalculations", new PagingCalculations(blockSearch.getPagingOptions(false), output))
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
	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public StorageBlock get(
		@Context SecurityContext securityContext,
		@PathParam String id
	) {
		return super.get(securityContext, id);
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
	@RolesAllowed(UserRoles.INVENTORY_EDIT)
	@Produces(MediaType.APPLICATION_JSON)
	public StorageBlock update(
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
	@RolesAllowed(UserRoles.INVENTORY_EDIT)
	@Produces(MediaType.APPLICATION_JSON)
	public StorageBlock delete(
		@Context SecurityContext securityContext,
		@PathParam String id
	) {
		return super.delete(securityContext, id);
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
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public StorageBlockTree tree(
		@Context SecurityContext securityContext,
		//for actual queries
		@QueryParam("onlyInclude") List<ObjectId> onlyInclude
	) {
		logRequestContext(this.getJwt(), securityContext);
		return ((StorageBlockService) this.getObjectService()).getStorageBlockTree(onlyInclude);
	}
	
	
	//<editor-fold desc="History">
	@GET
	@Path("{id}/history")
	@Operation(
		summary = "Gets a particular Storage Block's history."
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
	@RolesAllowed(UserRoles.INVENTORY_VIEW)
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
		summary = "Searches the history for the Storage Blocks."
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
	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public SearchResult<ObjectHistory> searchHistory(
		@Context SecurityContext securityContext,
		@BeanParam HistorySearch searchObject
	) {
		return super.searchHistory(securityContext, searchObject);
	}
	//</editor-fold>
}
