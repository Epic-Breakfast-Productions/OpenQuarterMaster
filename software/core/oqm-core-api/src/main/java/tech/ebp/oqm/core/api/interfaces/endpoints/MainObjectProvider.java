package tech.ebp.oqm.core.api.interfaces.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;

import java.util.List;

/**
 * Main abstract method to handle standard CRUD operations for MainObjects
 * <p>
 *
 * @param <T>
 * @param <S>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MainObjectProvider<T extends MainObject, S extends SearchObject<T>> extends ObjectProvider {
	
	@Getter
	@PathParam("oqmDbIdOrName")
	String oqmDbIdOrName;
	
	@Getter
	@Inject
	ObjectMapper objectMapper;
	
	public abstract Class<T> getObjectClass();
	
	public abstract MongoHistoriedObjectService<T, S, ?> getObjectService();
	
	//<editor-fold desc="CRUD operations">
	
	//	@POST
	//	@Operation(
	//		summary = "Adds a new object."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object added.",
	//		content = @Content(
	//			mediaType = "application/json",
	//			schema = @Schema(
	//				implementation = ObjectId.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@RolesAllowed(UserRoles.INVENTORY_EDIT)
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	public T create(
		@NotNull @Valid T object
	) {
		log.info("Creating new {} ({}) from REST interface.", this.getObjectClass().getSimpleName(), object.getClass());
		
		T newObj = this.getObjectService().add(this.getOqmDbIdOrName(), object, this.getInteractingEntity());
		
		log.info("{} created with id: {}", this.getObjectClass().getSimpleName(), newObj.getId());
		return object;
	}
	
	public List<T> createBulk(
		@Valid List<T> objects
	) {
		log.info("Creating new {} (bulk) from REST interface.", this.getObjectClass().getSimpleName());
		
		List<T> output = this.getObjectService().addBulk(this.getOqmDbIdOrName(), objects, this.getInteractingEntity());
		log.info("{} {} created with ids: {}", output.size(), this.getObjectClass().getSimpleName(), output);
		return output;
	}
	
	protected Response.ResponseBuilder getSearchResponseBuilder(S searchObject) {
		SearchResult<T> searchResult = this.getObjectService().search(this.getOqmDbIdOrName(), searchObject, false);
		return this.getSearchResultResponseBuilder(searchResult);
	}
	
	//	@GET
	//	@Operation(
	//		summary = "Gets a list of objects, using search parameters."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Blocks retrieved.",
	//		content = {
	//			@Content(
	//				mediaType = "application/json",
	//				schema = @Schema(
	//					type = SchemaType.ARRAY,
	//					implementation = MainObject.class
	//				)
	//			)
	//		},
	//		headers = {
	//			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
	//			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
	//		}
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public SearchResult<T> search(
		//		@BeanParam
		S searchObject
	) {
		return this.getObjectService().search(this.getOqmDbIdOrName(), searchObject);
	}
	
	//	@Path("{id}")
	//	@GET
	//	@Operation(
	//		summary = "Gets a particular object."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object retrieved.",
	//		content = @Content(
	//			mediaType = "application/json",
	//			schema = @Schema(
	//				implementation = MainObject.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "Bad request given, could not find object at given id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "410",
	//		description = "Object requested has been deleted.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public T get(
		//		@PathParam("id")
		String id
	) {
		log.info("Retrieving {} from REST interface with id {}", this.getObjectClass().getSimpleName(), id);
		
		log.info("Retrieving object with id {}", id);
		T output = this.getObjectService().get(this.getOqmDbIdOrName(), id);
		
		log.info("{} found with id {}", this.getObjectClass().getSimpleName(), id);
		return output;
	}
	
//	@Path("stats")
//	@GET
//	@Operation(
//		summary = "Gets stats on this object's collection."
//	)
//	@APIResponse(
//		responseCode = "200",
//		description = "Object retrieved.",
//		content = @Content(
//			mediaType = "application/json",
//			schema = @Schema(
//				implementation = CollectionStats.class
//			)
//		)
//	)
//	@Produces(MediaType.APPLICATION_JSON)
//	@RolesAllowed(Roles.INVENTORY_VIEW)
//	@WithSpan
	public CollectionStats getCollectionStats(
	) {
		return this.getObjectService().getStats(this.getOqmDbIdOrName());
	}
	
	//	@PUT
	//	@Path("{id}")
	//	@Operation(
	//		summary = "Updates a particular Object.",
	//		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object updated.",
	//		content = @Content(
	//			mediaType = "application/json",
	//			schema = @Schema(
	//				implementation = MainObject.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "Bad request given, could not find object at given id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "410",
	//		description = "Object requested has been deleted.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@RolesAllowed(UserRoles.INVENTORY_EDIT)
	//	@Produces(MediaType.APPLICATION_JSON)
	public T update(
		//		@PathParam("id")
		String id,
		ObjectNode updates
	) {
		log.info("Updating {} from REST interface with id {}", this.getObjectClass().getSimpleName(), id);
		
		T updated = this.getObjectService().update(this.getOqmDbIdOrName(), null, id, updates, this.getInteractingEntity());
		
		log.info("Updated {} with id {}", updated.getClass().getSimpleName(), id);
		return updated;
	}
	
	//	@DELETE
	//	@Path("{id}")
	//	@Operation(
	//		summary = "Deletes a particular object."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object deleted.",
	//		content = @Content(
	//			mediaType = "application/json",
	//			schema = @Schema(
	//				implementation = MainObject.class
	//			)
	//		)
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "Bad request given, could not find object at given id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "410",
	//		description = "Object requested has already been deleted.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@RolesAllowed(UserRoles.INVENTORY_EDIT)
	//	@Produces(MediaType.APPLICATION_JSON)
	public T delete(
		//		@PathParam("id")
		String id
	) {
		log.info("Deleting {} with id {} from REST interface.", this.getObjectClass().getSimpleName(), id);
		T output = this.getObjectService().remove(this.getOqmDbIdOrName(), id, this.getInteractingEntity());
		
		log.info("{} found, deleted.", output.getClass().getSimpleName());
		return output;
	}
	//</editor-fold>
	
	//<editor-fold desc="History">
	
	//	@GET
	//	@Path("{id}/history")
	//	@Operation(
	//		summary = "Gets a particular object's history."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Object retrieved.",
	//		content = {
	//			@Content(
	//				mediaType = "application/json",
	//				schema = @Schema(type = SchemaType.ARRAY, implementation = ObjectHistoryEvent.class)
	//			)
	//		}
	//	)
	//	@APIResponse(
	//		responseCode = "400",
	//		description = "Bad request given. Data given could not pass validation.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "No history found for object with that id.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response getHistoryForObject(
		@PathParam("id") String id,
		//@BeanParam
		HistorySearch searchObject
	) {
		log.info("Retrieving specific {} history with id {} from REST interface", this.getObjectClass().getSimpleName(), id);
		
		searchObject.setObjectId(new ObjectId(id));
		SearchResult<ObjectHistoryEvent> searchResult = this.getObjectService().searchHistory(this.getOqmDbIdOrName(), searchObject, false);
		
		log.info("Found {} history events matching query.", searchResult.getNumResultsForEntireQuery());
		
		Response.ResponseBuilder rb = this.getSearchResultResponseBuilder(searchResult);
		return rb.build();
	}
	
	//	@GET
	//	@Path("history")
	//	@Operation(
	//		summary = "Searches the history for the objects."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Blocks retrieved.",
	//		content = {
	//			@Content(
	//				mediaType = "application/json",
	//				schema = @Schema(
	//					type = SchemaType.ARRAY,
	//					implementation = ObjectHistoryEvent.class
	//				)
	//			)
	//		},
	//		headers = {
	//			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
	//			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
	//		}
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	public SearchResult<ObjectHistoryEvent> searchHistory(
		//@BeanParam
		HistorySearch searchObject
	) {
		log.info("Searching for history with: {}", searchObject);
		
		return this.getObjectService().searchHistory(this.getOqmDbIdOrName(), searchObject, false);
	}
	//</editor-fold>
}
