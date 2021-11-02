package com.ebp.openQuarterMaster.baseStation.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.data.pojos.UserCreateRequest;
import com.ebp.openQuarterMaster.baseStation.data.pojos.UserLoginResponse;
import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import com.ebp.openQuarterMaster.baseStation.service.PasswordService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Traced
@Slf4j
@Path("/user/auth")
@Tags({@Tag(name = "Users"), @Tag(name = "User Auth")})
@RequestScoped
public class Auth extends EndpointProvider {
    @Inject
    UserService service;
    @Inject
    JsonWebToken jwt;
    @Inject
    PasswordService passwordService;


    @POST
    @Operation(
            summary = "Authenticates a user"
    )
    @APIResponse(
            responseCode = "202",
            description = "User was logged in.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class)
            )
    )
    @APIResponse(
            responseCode = "401",
            description = "Incorrect credentials given.",
            content = @Content(mediaType = "text/plain")
    )
    @APIResponse(
            responseCode = "403",
            description = "If the account has been locked.",
            content = @Content(mediaType = "text/plain")
    )
    @APIResponse(
            responseCode = "429",
            description = "Happens when too many requests to login were sent in a given time period.",
            content = @Content(mediaType = "text/plain")
    )
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateUser(
            @Context SecurityContext securityContext,
            @Valid UserCreateRequest userCreateRequest
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Authenticating user.");


        return Response.status(Response.Status.ACCEPTED)
//                .entity(output)
                .build();
    }
}