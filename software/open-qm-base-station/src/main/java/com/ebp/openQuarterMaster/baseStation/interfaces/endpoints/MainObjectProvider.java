package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints;

import com.ebp.openQuarterMaster.baseStation.rest.search.HistorySearch;
import com.ebp.openQuarterMaster.baseStation.rest.search.SearchObject;
import com.ebp.openQuarterMaster.baseStation.service.mongo.MongoHistoriedService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.ebp.openQuarterMaster.lib.core.history.ObjectHistory;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Main abstract method to handle standard CRUD operations for MainObjects
 * <p>
 *
 * @param <T>
 * @param <S>
 */
@Traced
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MainObjectProvider<T extends MainObject, S extends SearchObject<T>> extends EndpointProvider {
	
	@Getter
	private Class<T> objectClass;
	@Getter
	private MongoHistoriedService<T, S> objectService;
	@Getter
	private UserService userService;
	@Getter
	private JsonWebToken jwt;
	private User userFromJwt = null;
	@Getter
	private Template historyRowsTemplate;
	
	protected MainObjectProvider(
		Class<T> objectClass,
		MongoHistoriedService<T, S> objectService,
		UserService userService,
		JsonWebToken jwt,
		Template historyRowsTemplate
	) {
		this.objectClass = objectClass;
		this.objectService = objectService;
		this.userService = userService;
		this.jwt = jwt;
		this.historyRowsTemplate = historyRowsTemplate;
	}
	
	protected User getUserFromJwt() {
		if (this.userFromJwt == null) {
			this.userFromJwt = this.getUserService().getFromJwt(this.getJwt());
		}
		return this.userFromJwt;
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
	//	@RolesAllowed("user")
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	public ObjectId create(
		@Context SecurityContext securityContext,
		@Valid T object
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Creating new {} ({}) from REST interface.", this.getObjectClass().getSimpleName(), object.getClass());
		
		ObjectId output = this.getObjectService().add(object, this.getUserFromJwt());
		log.info("{} created with id: {}", this.getObjectClass().getSimpleName(), output);
		return output;
	}
	
	protected Response.ResponseBuilder getSearchResultResponseBuilder(SearchResult<?> searchResult) {
		return Response.status(Response.Status.OK)
					   .entity(searchResult.getResults())
					   .header("num-elements", searchResult.getResults().size())
					   .header("query-num-results", searchResult.getNumResultsForEntireQuery());
	}
	
	protected Tuple2<Response.ResponseBuilder, SearchResult<T>> getSearchResponseBuilder(
		@Context SecurityContext securityContext,
		@BeanParam S searchObject
	) {
		logRequestContext(this.getJwt(), securityContext);
		
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
	//	@RolesAllowed("user")
	public Response search(
		@Context SecurityContext securityContext,
		@BeanParam S searchObject
	) {
		return this.getSearchResponseBuilder(securityContext, searchObject).getItem1().build();
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
	//	@RolesAllowed("user")
	public T get(
		@Context SecurityContext securityContext,
		@PathParam String id
	) {
		logRequestContext(this.getJwt(), securityContext);
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
	//	@RolesAllowed("user")
	//	@Produces(MediaType.APPLICATION_JSON)
	public T update(
		@Context SecurityContext securityContext,
		@PathParam String id,
		ObjectNode updates
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Updating {} from REST interface with id {}", this.getObjectClass().getSimpleName(), id);
		
		T updated = this.getObjectService().update(id, updates, this.getUserFromJwt());
		
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
	//	@RolesAllowed("user")
	//	@Produces(MediaType.APPLICATION_JSON)
	public T delete(
		@Context SecurityContext securityContext,
		@PathParam String id
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Deleting {} with id {} from REST interface.", this.getObjectClass().getSimpleName(), id);
		T output = this.getObjectService().remove(id, this.getUserFromJwt());
		
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
//				schema = @Schema(implementation = ObjectHistory.class)
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
//	@RolesAllowed("user")
	public Response getHistoryForObject(
		@Context SecurityContext securityContext,
		@PathParam String id,
		@HeaderParam("accept") String acceptHeaderVal
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Retrieving specific {} history with id {} from REST interface", this.getObjectClass().getSimpleName(), id);
		
		log.info("Retrieving object with id {}", id);
		ObjectHistory output = this.getObjectService().getHistoryFor(id);
		
		log.info("History found with id {} for {} of id {}", output.getId(), this.getObjectClass().getSimpleName(), id);
		
		Response.ResponseBuilder rb = Response.ok();
		log.debug("Accept header value: \"{}\"", acceptHeaderVal);
		switch (acceptHeaderVal) {
			case MediaType.TEXT_HTML:
				log.debug("Requestor wanted html.");
				rb = rb.entity(
						   this.getHistoryRowsTemplate()
							   .data("objectHistory", output)
							   .data("userService", this.getUserService())
					   )
					   .type(MediaType.TEXT_HTML_TYPE);
				break;
			case MediaType.APPLICATION_JSON:
			default:
				log.debug("Requestor wanted json, or any other form");
				rb = rb.entity(output)
					   .type(MediaType.APPLICATION_JSON_TYPE);
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
	//					implementation = ObjectHistory.class
	//				)
	//			)
	//		},
	//		headers = {
	//			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
	//			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
	//		}
	//	)
	//	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	//	@RolesAllowed("user")
	public SearchResult<ObjectHistory> searchHistory(
		@Context SecurityContext securityContext,
		@BeanParam HistorySearch searchObject
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Searching for objects with: {}", searchObject);
		
		return this.getObjectService().searchHistory(searchObject, false);
	}
	//</editor-fold>
}
