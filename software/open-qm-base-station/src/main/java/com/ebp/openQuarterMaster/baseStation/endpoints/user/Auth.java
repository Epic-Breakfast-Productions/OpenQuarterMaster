package com.ebp.openQuarterMaster.baseStation.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import com.ebp.openQuarterMaster.baseStation.restCalls.KeycloakServiceCaller;
import com.ebp.openQuarterMaster.baseStation.service.JwtService;
import com.ebp.openQuarterMaster.baseStation.service.PasswordService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.ebp.openQuarterMaster.baseStation.utils.TimeUtils;
import com.ebp.openQuarterMaster.lib.core.rest.ErrorMessage;
import com.ebp.openQuarterMaster.lib.core.rest.user.TokenCheckResponse;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginRequest;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginResponse;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.cache.NoCache;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Date;

@Traced
@Slf4j
@Path("/api/user/auth")
@Tags({@Tag(name = "User Auth", description = "Endpoints for user authorization.")})
@RequestScoped
@NoCache
public class Auth extends EndpointProvider {
    @Inject
    UserService userService;

    @Inject
    PasswordService passwordService;

    @Inject
    JwtService jwtService;

    @Inject
    @RestClient
    KeycloakServiceCaller keycloakServiceCaller;

    @ConfigProperty(name = "service.authMode")
    AuthMode authMode;

    @ConfigProperty(name = "service.externalAuth.clientId", defaultValue = "")
    String externalClientId;
    @ConfigProperty(name = "service.externalAuth.clientSecret", defaultValue = "")
    String externalClientSecret;
    @ConfigProperty(name = "service.externalAuth.scope", defaultValue = "")
    String externalScope;
    @ConfigProperty(name = "service.externalAuth.callbackUrl", defaultValue = "")
    String callbackUrl;

    @Inject
    JsonWebToken jwt;
    @Inject
    SecurityIdentity identity;

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
            responseCode = "400",
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
            @Valid UserLoginRequest loginRequest
    ) {
        logRequestContext(this.jwt, securityContext);
        assertSelfAuthMode(this.authMode);
        log.info("Authenticating user.");

        User user = this.userService.getFromLoginRequest(loginRequest);

        if (user == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("User not found.")).build();
        }

        if (!this.passwordService.passwordMatchesHash(user, loginRequest)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Invalid Password.")).build();
        }

        //TODO:: additional checks on locked status, etc

        log.info("User {} authenticated, generating token and returning.", user.getId());

        return Response.status(Response.Status.ACCEPTED)
                .entity(this.jwtService.getUserJwt(user, false))
                .build();
    }

    @GET
    @Path("tokenCheck")
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
    @SecurityRequirement(name = "JwtAuth")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response tokenCheck(@Context SecurityContext ctx) {
        logRequestContext(this.jwt, ctx);
        log.info("Checking user's token.");

        TokenCheckResponse response = new TokenCheckResponse();
        if (jwt.getRawToken() != null) {
            log.info("User roles: {}", this.identity.getRoles());
            log.info("User JWT claims: {}", this.jwt.getClaimNames());

            response.setHadToken(true);
            response.setTokenSecure(ctx.isSecure());
            response.setExpired(jwt.getExpirationTime() <= TimeUtils.currentTimeInSecs());
            response.setExpirationDate(new Date(jwt.getExpirationTime()));
        } else {
            log.info("User had no jwt");
        }
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(response).build();
    }

    @GET
    @Path("callback")
    @Operation(
            summary = "Callback for an external auth provider to come back to this service."
    )
    @APIResponse(
            responseCode = "303",
            description = "Token from external auth source received."
    )
    @APIResponse(
            responseCode = "403",
            description = "Service is not in external auth mode."
    )
    @PermitAll
    public Response callback(
            @Context SecurityContext ctx,
            @QueryParam("returnPath") String returnPath,
            @QueryParam("code") String code
    ) {
        //TODO:: check state info
        logRequestContext(this.jwt, ctx);
        log.info("Receiving token from external auth.");
        if (AuthMode.SELF.equals(authMode)) {
            //TODO:: throw custom exception, handle to return proper response object
            throw new ForbiddenException("Service not set to authenticate via external means.");
        }

        JsonNode returned;
        try {
            returned = this.keycloakServiceCaller.getJwt(
                    this.externalClientId,
                    this.externalClientSecret,
                    this.externalScope,
                    "authorization_code",
                    code,
                    this.callbackUrl
            );
        } catch (Throwable e) {
            log.warn("Failed to get token from keycloak (exception)- ", e);
            //TODO:: deal with properly
            e.printStackTrace();
            throw e;
        }

        if (!returned.has("access_token")) {
            log.warn("Failed to get token from keycloak (token not in data)");
            //TODO:: handle
            throw new IllegalStateException("Token not in data");
        }

        String jwt = returned.get("access_token").asText();

        log.debug("JWT got from external auth: {}", jwt);
        log.debug("Public key to verify sig: {}", ConfigProvider.getConfig().getValue("mp.jwt.verify.publickey.location", String.class));

        return Response.seeOther(
                        UriBuilder.fromUri(
                                        (returnPath == null || returnPath.isBlank() ? "/overview" : returnPath)
                                )
                                .build()
                ).cookie(
                        new NewCookie(
                                ConfigProvider.getConfig().getValue("mp.jwt.token.cookie", String.class),
                                jwt,
                                "/",
                                ConfigProvider.getConfig().getValue("runningInfo.hostname", String.class),
                                "Login jwt",
                                Integer.MAX_VALUE,
                                true
                        )
                )
                .build();
    }


    @GET
    @Path("logout")
    @Operation(
            summary = "Callback for an external auth provider to come back to this service."
    )
    @APIResponse(
            responseCode = "303",
            description = "Token from external auth source received."
    )
    @APIResponse(
            responseCode = "403",
            description = "Service is not in external auth mode."
    )
    @PermitAll
    public Response logout(
            @Context SecurityContext ctx,
            @QueryParam("returnPath") String returnPath
    ) {
        return Response.seeOther(
                        UriBuilder.fromUri("/?messageHeading=Logout Success!&message=You have logged out.&messageType=success")
                                .build()
                ).cookie(
                        new NewCookie(
                                ConfigProvider.getConfig().getValue("mp.jwt.token.cookie", String.class),
                                "",
                                "/",
                                ConfigProvider.getConfig().getValue("runningInfo.hostnamePort", String.class),
                                "Login jwt clear",
                                0,
                                true
                        )
                )
                .build();
    }
}