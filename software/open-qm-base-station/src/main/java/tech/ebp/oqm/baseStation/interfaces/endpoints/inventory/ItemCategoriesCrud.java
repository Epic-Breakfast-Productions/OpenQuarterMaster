package tech.ebp.oqm.baseStation.interfaces.endpoints.inventory;

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
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.baseStation.rest.search.CategoriesSearch;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.StorageBlockSearch;
import tech.ebp.oqm.baseStation.service.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.ItemCategoryService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.lib.core.object.storage.ItemCategory;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.tree.StorageBlockTree;
import tech.ebp.oqm.lib.core.rest.auth.roles.Roles;

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

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/inventory/item-categories")
@Tags({@Tag(name = "Item Categories", description = "Endpoints for managing Item Categories.")})
@RequestScoped
@NoArgsConstructor
public class ItemCategoriesCrud extends MainObjectProvider<ItemCategory, CategoriesSearch> {
	
	Template itemCategoriesSearchResultsTemplate;
	
	@Inject
	public ItemCategoriesCrud(
		ItemCategoryService itemCategoryService,
		InteractingEntityService interactingEntityService,
		JsonWebToken jwt,
		@Location("tags/objView/history/searchResults.html")
		Template historyRowsTemplate,
		@Location("tags/search/category/searchResults.html")
		Template itemCategoriesSearchResultsTemplate
	) {
		super(ItemCategory.class, itemCategoryService, interactingEntityService, jwt, historyRowsTemplate);
		this.itemCategoriesSearchResultsTemplate = itemCategoriesSearchResultsTemplate;
	}
	
	@POST
	@Operation(
		summary = "Adds a new Item Category."
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
	public ObjectId create(
		@Context SecurityContext securityContext,
		@Valid ItemCategory itemCategory
	) {
		return super.create(securityContext, itemCategory);
	}
	
	@POST
	@Path("bulk")
	@Operation(
		summary = "Adds new Item Categories."
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
		@Context SecurityContext securityContext,
		@Valid List<ItemCategory> itemCategories
	) {
		return super.createBulk(securityContext, itemCategories);
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
					implementation = ItemCategory.class
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
	@Override
	public Response search(
		@Context SecurityContext securityContext,
		@BeanParam CategoriesSearch categoriesSearch
	) {
		Tuple2<Response.ResponseBuilder, SearchResult<ItemCategory>> tuple = super.getSearchResponseBuilder(securityContext, categoriesSearch);
		Response.ResponseBuilder rb = tuple.getItem1();
		
		log.debug("Accept header value: \"{}\"", categoriesSearch.getAcceptHeaderVal());
		switch (categoriesSearch.getAcceptHeaderVal()) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				SearchResult<ItemCategory> output = tuple.getItem2();
				rb = rb.entity(
						   this.itemCategoriesSearchResultsTemplate
							   .data("searchResults", output)
							   .data("actionType", (
								   categoriesSearch.getActionTypeHeaderVal() == null || categoriesSearch.getActionTypeHeaderVal().isBlank() ? "full" :
									   categoriesSearch.getActionTypeHeaderVal()
							   ))
							   .data(
								   "searchFormId",
								   (
									   categoriesSearch.getSearchFormIdHeaderVal() == null || categoriesSearch.getSearchFormIdHeaderVal().isBlank() ?
										   "" :
										   categoriesSearch.getSearchFormIdHeaderVal()
								   )
							   )
							   .data(
								   "inputIdPrepend",
								   (
									   categoriesSearch.getInputIdPrependHeaderVal() == null || categoriesSearch.getInputIdPrependHeaderVal().isBlank() ?
										   "" :
										   categoriesSearch.getInputIdPrependHeaderVal()
								   )
							   )
							   .data(
								   "otherModalId",
								   (
									   categoriesSearch.getOtherModalIdHeaderVal() == null || categoriesSearch.getOtherModalIdHeaderVal().isBlank() ?
										   "" :
										   categoriesSearch.getOtherModalIdHeaderVal()
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
		summary = "Gets a particular Storage Block."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ItemCategory.class
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
	public ItemCategory get(
		@Context SecurityContext securityContext,
		@PathParam("id") String id
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
				implementation = ItemCategory.class
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
	public ItemCategory update(
		@Context SecurityContext securityContext,
		@PathParam("id") String id,
		ObjectNode updates
	) {
		return super.update(securityContext, id, updates);
	}
	
	@DELETE
	@Path("{id}")
	@Operation(
		summary = "Deletes a particular category."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object deleted.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ItemCategory.class
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
	public ItemCategory delete(
		@Context SecurityContext securityContext,
		@PathParam("id") String id
	) {
		return super.delete(securityContext, id);
	}
	
	//TODO:: this
//	@GET
//	@Path("tree")
//	@Operation(
//		summary = "Gets a tree of the item categories."
//	)
//	@APIResponse(
//		responseCode = "200",
//		description = "Tree retrieved.",
//		content = {
//			@Content(
//				mediaType = "application/json",
//				schema = @Schema(
//					implementation = StorageBlockTree.class
//				)
//			)
//		}
//	)
//	@APIResponse(
//		responseCode = "204",
//		description = "No items found from query given.",
//		content = @Content(mediaType = "text/plain")
//	)
//	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
//	@RolesAllowed(Roles.INVENTORY_VIEW)
//	public StorageBlockTree tree(
//		@Context SecurityContext securityContext,
//		//for actual queries
//		@QueryParam("onlyInclude") List<ObjectId> onlyInclude
//	) {
//		logRequestContext(this.getJwt(), securityContext);
//		return ((StorageBlockService) this.getObjectService()).getStorageBlockTree(onlyInclude);
//	}
	
	
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
		@org.jboss.resteasy.annotations.jaxrs.PathParam String id,
		@BeanParam HistorySearch searchObject,
		@HeaderParam("accept") String acceptHeaderVal,
		@HeaderParam("searchFormId") String searchFormId
	) {
		return super.getHistoryForObject(securityContext, id, searchObject, acceptHeaderVal, searchFormId);
	}
	
	@GET
	@Path("history")
	@Operation(
		summary = "Searches the history for the categories."
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
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@Context SecurityContext securityContext,
		@BeanParam HistorySearch searchObject
	) {
		return super.searchHistory(securityContext, searchObject);
	}
	
	//</editor-fold>
	
	//TODO:: this
//	@GET
//	@Path("{id}/children")
//	@Operation(
//		summary = "Gets children of a particular item category."
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
//		@Context SecurityContext securityContext,
//		@PathParam("id") String storageBlockId
//	) {
//		logRequestContext(this.getJwt(), securityContext);
//		log.info("Getting children of \"{}\"", storageBlockId);
//		return Response.ok(((StorageBlockService)this.getObjectService()).getChildrenIn(storageBlockId)).build();
//	}
}
