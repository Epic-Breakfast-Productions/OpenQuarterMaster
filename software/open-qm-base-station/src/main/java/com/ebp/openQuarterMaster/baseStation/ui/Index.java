package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

@Traced
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class Index extends UiProvider {

    @Inject
    Template index;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserService userService;

    @GET
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public TemplateInstance index(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(jwt, securityContext);

        return index.data("hasToken", hasJwt(jwt));
    }

    @GET
    @RolesAllowed("user")
    @Produces(MediaType.TEXT_PLAIN)
    public TemplateInstance get(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(jwt, securityContext);
        User user = userService.getFromJwt(jwt);

        return index
                .data("hasToken", hasJwt(jwt))
                .data("username", user.getUsername());
    }

}
