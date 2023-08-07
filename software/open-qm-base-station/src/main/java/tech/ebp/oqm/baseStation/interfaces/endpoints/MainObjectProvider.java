package tech.ebp.oqm.baseStation.interfaces.endpoints;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;

import javax.validation.Valid;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
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
	private Class<T> objectClass;
	@Getter
	private MongoHistoriedObjectService<T, S> objectService;
	@Getter
	private Template historyRowsTemplate;
	
	protected MainObjectProvider(
		JsonWebToken jwt,
		InteractingEntityService interactingEntityService,
		SecurityContext securityContext,
		Class<T> objectClass,
		MongoHistoriedObjectService<T, S> objectService,
		Template historyRowsTemplate
	) {
		super(jwt, interactingEntityService, securityContext);
		this.objectClass = objectClass;
		this.objectService = objectService;
		this.historyRowsTemplate = historyRowsTemplate;
	}
	
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
	@WithSpan
	public ObjectId create(
		@Valid T object
	) {
		log.info("Creating new {} ({}) from REST interface.", this.getObjectClass().getSimpleName(), object.getClass());
		
		ObjectId output;
		if (
			this.getObjectService().isAllowNullEntityForCreate()
			&& this.getJwt().getRawToken() == null
		) {
			output = this.getObjectService().add(object, null);
		} else {
			output = this.getObjectService().add(object, this.getInteractingEntity());
		}
		
		log.info("{} created with id: {}", this.getObjectClass().getSimpleName(), output);
		return output;
	}
	
	@WithSpan
	public List<ObjectId> createBulk(
		@Valid List<T> objects
	) {
		log.info("Creating new {} (bulk) from REST interface.", this.getObjectClass().getSimpleName());
		
		List<ObjectId> output = this.getObjectService().addBulk(objects, this.getInteractingEntity());
		log.info("{} {} created with ids: {}", output.size(), this.getObjectClass().getSimpleName(), output);
		return output;
	}
	
	protected Tuple2<Response.ResponseBuilder, SearchResult<T>> getSearchResponseBuilder(
		//		@BeanParam
		S searchObject
	) {
		SearchResult<T> searchResult = this.getObjectService().search(searchObject, false);
		
		return Tuple2.of(
			this.getSearchResultResponseBuilder(searchResult),
			searchResult
		);
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
	//			),
	//			@Content(
	//				mediaType = "text/html",
	//				schema = @Schema(type = SchemaType.STRING)
	//			)
	//		},
	//		headers = {
	//			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
	//			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
	//		}
	//	)
	//	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	@WithSpan
	public Response search(
		//		@BeanParam
		S searchObject
	) {
		return this.getSearchResponseBuilder(searchObject).getItem1().build();
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
	@WithSpan
	public T get(
		//		@PathParam("id")
		String id
	) {
		log.info("Retrieving {} from REST interface with id {}", this.getObjectClass().getSimpleName(), id);
		
		log.info("Retrieving object with id {}", id);
		T output = this.getObjectService().get(id);
		
		log.info("{} found with id {}", this.getObjectClass().getSimpleName(), id);
		return output;
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
	@WithSpan
	public T update(
		//		@PathParam("id")
		String id,
		ObjectNode updates
	) {
		log.info("Updating {} from REST interface with id {}", this.getObjectClass().getSimpleName(), id);
		
		T updated = this.getObjectService().update(id, updates, this.getInteractingEntity());
		
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
	//	@APIResponse(
	//		responseCode = "404",
	//		description = "No object found to delete.",
	//		content = @Content(mediaType = "text/plain")
	//	)
	//	@RolesAllowed(UserRoles.INVENTORY_EDIT)
	//	@Produces(MediaType.APPLICATION_JSON)
	@WithSpan
	public T delete(
		//		@PathParam("id")
		String id
	) {
		log.info("Deleting {} with id {} from REST interface.", this.getObjectClass().getSimpleName(), id);
		T output = this.getObjectService().remove(id, this.getInteractingEntity());
		
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
	//			),
	//			@Content(
	//				mediaType = "text/html",
	//				schema = @Schema(type = SchemaType.STRING)
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
	//	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	//	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public Response getHistoryForObject(
		@PathParam("id") String id,
		//@BeanParam
		HistorySearch searchObject,
		//		@HeaderParam("accept")
		String acceptHeaderVal,
		//		@HeaderParam("searchFormId")
		String searchFormId
	) {
		log.info("Retrieving specific {} history with id {} from REST interface", this.getObjectClass().getSimpleName(), id);
		
		searchObject.setObjectId(new ObjectId(id));
		
		SearchResult<ObjectHistoryEvent> searchResult = this.getObjectService().searchHistory(searchObject, false);
		
		
		log.info("Found {} history events matching query.", searchResult.getNumResultsForEntireQuery());
		
		Response.ResponseBuilder rb = this.getSearchResultResponseBuilder(searchResult);
		log.debug("Accept header value: \"{}\"", acceptHeaderVal);
		switch (acceptHeaderVal) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				rb = rb.entity(
						this.getHistoryRowsTemplate()
							.data("searchFormId", searchFormId)
							.data("searchResults", searchResult)
							.data("interactingEntityService", this.getInteractingEntityService())
							.data("pagingCalculations", new PagingCalculations(searchResult))
					)
						 .type(MediaType.TEXT_HTML_TYPE);
				break;
			case MediaType.APPLICATION_JSON:
			default:
				log.debug("Requestor wanted json, or any other form");
		}
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
	//	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	@WithSpan
	public SearchResult<ObjectHistoryEvent> searchHistory(
		//@BeanParam
		HistorySearch searchObject
	) {
		log.info("Searching for objects with: {}", searchObject);
		
		return this.getObjectService().searchHistory(searchObject, false);
	}
	//</editor-fold>
}
