package com.ebp.openQuarterMaster.baseStation.endpoints;

import com.ebp.openQuarterMaster.baseStation.service.JwtService;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginResponse;
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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Traced
@Slf4j
@Path("/api/demo")
@Tags({@Tag(name = "Demo", description = "Endpoints for Demo.")})
@RequestScoped
public class Demo extends EndpointProvider {
    @Inject
    JwtService jwtService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("one")
    @Operation(
            summary = "Test Endpoint 1"
    )
    @APIResponse(
            responseCode = "200",
            description = "User was logged in.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class)
            )
    )
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public Response one(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(this.jwt, securityContext);

        return Response.status(Response.Status.OK)
                .entity("one")
                .build();
    }

    @GET
    @Path("two")
    @Operation(
            summary = "Test Endpoint 2"
    )
    @APIResponse(
            responseCode = "200",
            description = "User was logged in.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class)
            )
    )
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public Response two(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(this.jwt, securityContext);

        return Response.status(Response.Status.OK)
                .entity("two")
                .build();
    }

    @GET
    @Path("three")
    @Operation(
            summary = "Test Endpoint 3"
    )
    @APIResponse(
            responseCode = "200",
            description = "User was logged in.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class)
            )
    )
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public Response three(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(this.jwt, securityContext);

        return Response.status(Response.Status.OK)
                .entity("three")
                .build();
    }


    @GET
    @Path("four")
    @Operation(
            summary = "Test Endpoint 4"
    )
    @APIResponse(
            responseCode = "200",
            description = "User was logged in.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class)
            )
    )
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public Response four(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(this.jwt, securityContext);

        return Response.status(Response.Status.OK)
                .entity("four")
                .build();
    }
}