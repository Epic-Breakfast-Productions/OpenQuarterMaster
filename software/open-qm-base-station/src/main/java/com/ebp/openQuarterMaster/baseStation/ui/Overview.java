package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.demo.DemoServiceCaller;
import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;

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
public class Overview extends UiProvider {

    @Inject
    @Location("webui/pages/overview")
    Template overview;

    @Inject
    UserService userService;
    @Inject
    InventoryItemService inventoryItemService;
    @Inject
    StorageBlockService storageBlockService;

    @Inject
    JsonWebToken jwt;

    @Inject
    @RestClient
    DemoServiceCaller extensionsService;

    @GET
    @Path("overview")
    @RolesAllowed("user")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance overview(
            @Context SecurityContext securityContext
    ) {
        logRequestContext(jwt, securityContext);
        User user = userService.getFromJwt(this.jwt);

        //FOR DEMO PURPOSES ONLY
        String response1 = null;
        String response2 = null;
        {
            String authHeaderContent = "Bearer " + this.jwt.getRawToken();

            try {
                response1 = extensionsService.get1(authHeaderContent);
//                response1 = extensionsService.get1(authHeaderContent);
            } catch (Throwable e){
                log.warn("Failed to reach service for 1: ", e);
                response1 = e.getMessage();
            }
            try {
                response2 = extensionsService.get2(authHeaderContent);
            } catch (Throwable e){
                log.warn("Failed to reach service for 2: ", e);
                response2 = e.getMessage();
            }
        }

        return overview
                .data(USER_INFO_DATA_KEY, UserGetResponse.builder(user).build())
                .data("numItems", inventoryItemService.count())
                .data("numStorageBlocks", storageBlockService.count())
                .data("response1", response1)
                .data("response2", response2)
                ;
    }

}
