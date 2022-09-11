package tech.ebp.oqm.baseStation.interfaces.endpoints.user;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import tech.ebp.oqm.baseStation.interfaces.endpoints.MainObjectProvider;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.UserSearch;
import tech.ebp.oqm.baseStation.service.PasswordService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.utils.AuthMode;
import tech.ebp.oqm.baseStation.utils.UserRoles;
import tech.ebp.oqm.lib.core.object.history.ObjectHistory;
import tech.ebp.oqm.lib.core.object.user.User;
import tech.ebp.oqm.lib.core.rest.user.UserCreateRequest;
import tech.ebp.oqm.lib.core.rest.user.UserGetResponse;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Traced
@Slf4j
@Path("/api/user")
@Tags({@Tag(name = "Users", description = "Endpoints for user CRUD")})
@RequestScoped
public class UserCrud extends MainObjectProvider<User, UserSearch> {
	
	PasswordService passwordService;
	AuthMode authMode;
	
	@Inject
	public UserCrud(
		UserService userService,
		JsonWebToken jwt,
		@Location("tags/objView/objHistoryViewRows.html")
		Template historyRowsTemplate,
		PasswordService passwordService,
		@ConfigProperty(name = "service.authMode")
		AuthMode authMode
	) {
		super(User.class, userService, userService, jwt, historyRowsTemplate);
		this.passwordService = passwordService;
		this.authMode = authMode;
	}
	
	
	
	
	@POST
	@Operation(
		summary = "Adds a new user. Only for use when AuthMode set to SELF"
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
	@PermitAll
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObjectId create(
		@Context SecurityContext securityContext,
		@Valid UserCreateRequest ucr
	) {
		assertSelfAuthMode(this.authMode);
		log.info("Creating new user.");
		User.Builder builder = User.builder(ucr);
		
		{
			Set<String> roles = new HashSet<>() {{
				add(UserRoles.USER);
				add(UserRoles.INVENTORY_VIEW);
			}};
			if (this.getUserService().collectionEmpty()) {
				log.info("New user is the first user to enter system. Making them an admin.");
				builder.disabled(false);
				roles.add(UserRoles.USER_ADMIN);
				roles.add(UserRoles.INVENTORY_EDIT);
				roles.add(UserRoles.INVENTORY_ADMIN);
			} else {
				log.info("New user is not the first. Disabling.");
				builder.disabled(true);
			}
			builder.roles(roles);
		}
		
		builder.pwHash(this.passwordService.createPasswordHash(ucr.getPassword()));
		
		User newUser = builder.build();
		if (ucr.getAttributes() != null) {
			newUser.getAttributes().putAll(ucr.getAttributes());
		}
		
		//		TODO:: test to see if we need this
		//		Set<ConstraintViolation<User>> validationViolations = validator.validate(newUser);
		//		if (!validationViolations.isEmpty()) {
		//			Response.status(Response.Status.BAD_REQUEST).entity(validationViolations).build();
		//		}
		
		return super.create(securityContext, newUser);
	}
	
	
	@GET
	@Operation(
		summary = "Gets a list of users, using search parameters."
	)
	@APIResponse(
		responseCode = "200",
		description = "Blocks retrieved.",
		content = {
			@Content(
				mediaType = "application/json",
				schema = @Schema(
					type = SchemaType.ARRAY,
					implementation = UserGetResponse.class
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
	@RolesAllowed("userAdmin")
	public Response search(
		@Context SecurityContext securityContext,
		@BeanParam UserSearch searchObject
	) {
		Tuple2<Response.ResponseBuilder, SearchResult<User>> results = this.getSearchResponseBuilder(securityContext, searchObject);
		SearchResult<User> originalResult = results.getItem2();
		
		SearchResult<UserGetResponse> output = new SearchResult<>(
			results.getItem2().getResults()
				   .stream()
				   .map((User user)->{
					   return UserGetResponse.builder(user).build();
				   })
				   .collect(Collectors.toList()),
			originalResult.getNumResultsForEntireQuery(),
			originalResult.isHadSearchQuery()
		);
		
		return this.getSearchResultResponseBuilder(output).build();
	}
	
	
	@PUT
	@Path("{id}")
	@Operation(
		summary = "Updates a particular User.",
		description = "Partial update to a object. Do not need to supply all fields, just the one(s) you wish to update."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object updated.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = User.class
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
	@RolesAllowed(UserRoles.USER_ADMIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public User update(
		@Context SecurityContext securityContext,
		@PathParam String id,
		ObjectNode updates
	) {
		return super.update(securityContext, id, updates);
	}
	
	
	@Path("{id}")
	@GET
	@Operation(
		summary = "Gets a particular user."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = UserGetResponse.class
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
	@RolesAllowed("userAdmin")
	public UserGetResponse getUser(
		@Context SecurityContext securityContext,
		@PathParam String id
	) {
		return UserGetResponse.builder(this.get(securityContext, id)).build();
	}
	
	
	@GET
	@Path("self")
	@Operation(
		summary = "Gets information on the user supplied by the JWT."
	)
	@APIResponse(
		responseCode = "200",
		description = "User info retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				type = SchemaType.ARRAY,
				implementation = UserGetResponse.class
			)
		)
	)
	@APIResponse(
		responseCode = "204",
		description = "No users found from query given.",
		content = @Content(mediaType = "text/plain")
	)
	@PermitAll
	@SecurityRequirement(name = "JwtAuth")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getSelfInfo(
		@Context SecurityContext securityContext
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Retrieving info for user.");
		User user = this.getUserService().getFromJwt(this.getJwt());
		
		return Response
				   .status(Response.Status.OK)
				   .entity(UserGetResponse.builder(user).build())
				   .build();
	}
	
	
	@GET
	@Path("{id}/history")
	@Operation(
		summary = "Gets a particular User's history."
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
	@RolesAllowed("userAdmin")
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
		summary = "Searches the history for the users."
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
	@RolesAllowed("userAdmin")
	public SearchResult<ObjectHistory> searchHistory(
		@Context SecurityContext securityContext,
		@BeanParam HistorySearch searchObject
	) {
		logRequestContext(this.getJwt(), securityContext);
		log.info("Searching for objects with: {}", searchObject);
		
		return this.getObjectService().searchHistory(searchObject, false);
	}
	
	//TODO:: update self
}
