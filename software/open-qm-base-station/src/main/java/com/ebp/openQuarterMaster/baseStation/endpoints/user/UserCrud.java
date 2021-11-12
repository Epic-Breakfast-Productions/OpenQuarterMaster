package com.ebp.openQuarterMaster.baseStation.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import com.ebp.openQuarterMaster.baseStation.service.PasswordService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SortType;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserCreateRequest;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.user.User;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
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

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

@Traced
@Slf4j
@Path("/api/user")
@Tags({@Tag(name = "Users", description = "Endpoints for user CRUD")})
@RequestScoped
public class UserCrud extends EndpointProvider {
    @Inject
    Validator validator;
    @Inject
    UserService userService;
    @Inject
    JsonWebToken jwt;
    @Inject
    PasswordService passwordService;

    @POST
    @Operation(
            summary = "Adds a new user."
    )
    @APIResponse(
            responseCode = "201",
            description = "User added.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = ObjectId.class
                    )
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad request given. Data given could not pass validation.)",
            content = @Content(mediaType = "text/plain")
    )
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(
            @Context SecurityContext securityContext,
            @Valid UserCreateRequest userCreateRequest
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Creating new user.");

        //TODO:: don't do if authType is external


        //TODO:: refactor
        if (
                !this.userService.list(eq("email", userCreateRequest.getEmail()), null, null).isEmpty()
        ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User with email already exists.").build();
        }
        if (
                !this.userService.list(eq("username", userCreateRequest.getUsername()), null, null).isEmpty()
        ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User with username already exists.").build();
        }

        User.Builder builder = User.builder(userCreateRequest);

        {
            List<String> roles = new ArrayList<>() {{
                add("user");
            }};
            if (this.userService.collectionEmpty()) {
                roles.add("userAdmin");
            }
            builder.roles(roles);
        }

        builder.pwHash(this.passwordService.createPasswordHash(userCreateRequest.getPassword()));

        User newUser = builder.build();
        if (userCreateRequest.getAttributes() != null) {
            newUser.getAttributes().putAll(userCreateRequest.getAttributes());
        }

        Set<ConstraintViolation<User>> validationViolations = validator.validate(newUser);
        if (!validationViolations.isEmpty()) {
            Response.status(Response.Status.BAD_REQUEST).entity(validationViolations).build();
        }

        ObjectId output = userService.add(newUser, null);
        log.info("User created with id: {}", output);
        return Response.status(Response.Status.CREATED).entity(output).build();
    }

    @GET
    @Operation(
            summary = "Gets a list of users."
    )
    @APIResponse(
            responseCode = "200",
            description = "Users retrieved.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            type = SchemaType.ARRAY,
                            implementation = UserGetResponse.class
                    )
            ),
            headers = {
                    @Header(name="num-elements", description = "Gives the number of elements returned in the body."),
                    @Header(name = "query-num-results", description = "Gives the number of results in the query given.")
            }
    )
    @APIResponse(
            responseCode = "204",
            description = "No users found from query given.",
            content = @Content(mediaType = "text/plain")
    )
    @RolesAllowed("userAdmin")
    @Produces({MediaType.APPLICATION_JSON})
    public Response listUsers(
            @Context SecurityContext securityContext,
            //for actual queries
            @QueryParam("name") String name,
            //paging
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("pageNum") Integer pageNum,
            //sorting
            @QueryParam("sortBy") String sortField,
            @QueryParam("sortType") SortType sortType
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Searching for users with: ");

        List<Bson> filters = new ArrayList<>();
        Bson sort = SearchUtils.getSortBson(sortField, sortType);
        PagingOptions pageOptions = PagingOptions.fromQueryParams(pageSize, pageNum, false);

        if (name != null && !name.isBlank()) {
            //TODO:: handle first and last name properly
            filters.add(regex("firstName", SearchUtils.getSearchTermPattern(name)));
        }
        Bson filter = (filters.isEmpty() ? null : and(filters));

        List<User> users = this.userService.list(
                filter,
                sort,
                pageOptions
        );
        if(users.isEmpty()){
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        List<UserGetResponse> output = users
                .stream()
                .map((User user)->{
                    return UserGetResponse.builder(user).build();
                })
                .collect(Collectors.toList());


        return Response
                .status(Response.Status.OK)
                .entity(output)
                .header("num-elements", output.size())
                .header("query-num-results", this.userService.count(filter))
                .build();
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
        logRequestContext(this.jwt, securityContext);
        log.info("Retrieving info for user.");
        User user = this.userService.getFromJwt(jwt);

        return Response
                .status(Response.Status.OK)
                .entity(UserGetResponse.builder(user).build())
                .build();
    }

}
