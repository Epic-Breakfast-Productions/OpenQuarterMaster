package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints;

import com.ebp.openQuarterMaster.baseStation.rest.search.SearchObject;
import com.ebp.openQuarterMaster.baseStation.service.mongo.MongoHistoriedService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.Getter;
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
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Main abstract method to handle standard CRUD operations for MainObjects
 *
 * TODO:: proper typing on OpenApi docs for each type of object
 * @param <T>
 * @param <S>
 */
@Traced
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MainObjectProvider<T extends MainObject, S extends SearchObject<T>> extends EndpointProvider {
	
	@Getter
	private MongoHistoriedService<T, S> objectService;
	@Getter
	private UserService userService;
	@Getter
	private JsonWebToken jwt;
	private User userFromJwt = null;
	
	protected MainObjectProvider(MongoHistoriedService<T, S> objectService, UserService userService, JsonWebToken jwt) {
		this.objectService = objectService;
		this.userService = userService;
		this.jwt = jwt;
	}
	
	protected User getUserFromJwt(){
		if(this.userFromJwt == null) {
			this.userFromJwt = this.getUserService().getFromJwt(this.getJwt());
		}
		return this.userFromJwt;
	}
	
	@POST
	@Operation(
		summary = "Adds a new object."
	)
	@APIResponse(
		responseCode = "201",
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
	public abstract String create(
		@Context SecurityContext securityContext,
		@Valid T storageBlock
	);
	
	@GET
	@Operation(
		summary = "Gets a list of objects."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					type = SchemaType.ARRAY,
					implementation = MainObject.class
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
	public abstract Response search(
		@Context SecurityContext securityContext,
		@BeanParam S searchObject
	);
	
	@GET
	@Path("{id}")
	@Operation(
		summary = "Gets a particular object."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json"
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("user")
	public T get(
		@Context SecurityContext securityContext,
		@PathParam String id
	){
		logRequestContext(this.getJwt(), securityContext);
		log.info("Retrieving object with id {}", id);
		T output = this.getObjectService().get(id);
		
		log.info("Object found");
		return output;
	}
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular Object.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Storage block updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = MainObject.class
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
	public T update(
		@Context SecurityContext securityContext,
		@PathParam String id,
		ObjectNode updates
	){
		logRequestContext(this.getJwt(), securityContext);
		log.info("Updating object with id {}", id);
		
		T updated = this.getObjectService().update(id, updates, this.getUserFromJwt());
		
		return updated;
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
		description = "No object found to delete.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed("user")
	@Produces(MediaType.APPLICATION_JSON)
	public T delete(
		@Context SecurityContext securityContext,
		@PathParam String id
	){
		logRequestContext(this.getJwt(), securityContext);
		log.info("Deleting object with id {}", id);
		T output = this.getObjectService().remove(id, this.getUserFromJwt());
		
		log.info("Storage block found, deleted.");
		return output;
	}
	
	//TODO:: history functionality. Get hist for obj, search history?
//	public ObjectHistory getHistory(
//		@Context SecurityContext securityContext,
//		@PathParam String id
//	){
//		this.getObjectService().get
//	}
}
