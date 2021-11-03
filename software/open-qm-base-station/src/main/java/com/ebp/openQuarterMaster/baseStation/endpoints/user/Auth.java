package com.ebp.openQuarterMaster.baseStation.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.data.pojos.TokenCheckResponse;
import com.ebp.openQuarterMaster.baseStation.data.pojos.UserCreateRequest;
import com.ebp.openQuarterMaster.baseStation.data.pojos.UserLoginResponse;
import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import com.ebp.openQuarterMaster.baseStation.service.PasswordService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.ebp.openQuarterMaster.baseStation.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Date;

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

    @ConfigProperty(name = "service.authMode")
    AuthMode authMode;

    private void assertSelfAuthMode() {
        if (!AuthMode.SELF.equals(this.authMode)) {
            throw new ForbiddenException("Service not set to authenticate its own users.");
        }
    }

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
        this.assertSelfAuthMode();
        log.info("Authenticating user.");


        return Response.status(Response.Status.ACCEPTED)
//                .entity(output)
                .build();
    }

    @GET
    @Operation(
            summary = "Checks a users' token."
    )
    @APIResponse(
            responseCode = "200",
            description = "The check happened.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TokenCheckResponse.class)
            )
    )
    @Tags({@Tag(name = "User"), @Tag(name = "Auth")})
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response tokenCheck(@Context SecurityContext ctx) {
        logRequestContext(this.jwt, ctx);
        this.assertSelfAuthMode();
        TokenCheckResponse response = new TokenCheckResponse();
        if (jwt.getRawToken() != null) {
            response.setHadToken(true);
            response.setTokenSecure(ctx.isSecure());
            response.setExpired(jwt.getExpirationTime() <= TimeUtils.currentTimeInSecs());
            response.setExpirationDate(new Date(jwt.getExpirationTime()));
        }
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(response).build();
    }
}