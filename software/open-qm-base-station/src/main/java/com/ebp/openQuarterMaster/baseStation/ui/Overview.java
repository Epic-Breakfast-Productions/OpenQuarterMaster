package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.demo.DemoExternalServiceCaller;
import com.ebp.openQuarterMaster.baseStation.demo.DemoServiceCaller;
import com.ebp.openQuarterMaster.baseStation.restCalls.KeycloakServiceCaller;
import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    KeycloakServiceCaller ksc;

    @Inject
    @RestClient
    DemoServiceCaller demoService;
    @Inject
    @RestClient
    DemoExternalServiceCaller externDemoService;

    @GET
    @Path("overview")
    @RolesAllowed("user")
    @Produces(MediaType.TEXT_HTML)
    public Response overview(
            @Context SecurityContext securityContext,
            @CookieParam("jwt_refresh") String refreshToken
    ) {
        logRequestContext(jwt, securityContext);
        User user = userService.getFromJwt(this.jwt);
        List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(refreshAuthToken(ksc, refreshToken));

        //FOR DEMO PURPOSES ONLY
        String response1 = null;
        String response2 = null;
        String responseExt1 = null;
        String responseExt2 = null;
        {
            String authHeaderContent = "Bearer " + this.jwt.getRawToken();
            log.info("Performing rest calls to demo services.");
            {
                java.util.Map<Integer, CompletableFuture<String>> completionStages = new HashMap<>(4);

                if (ConfigProvider.getConfig().getValue("quarkus.rest-client.demoService.perform", Boolean.class)) {
                    completionStages.put(1, demoService.get1(authHeaderContent).toCompletableFuture());
                    completionStages.put(2, demoService.get2(authHeaderContent).toCompletableFuture());
                }
                if (ConfigProvider.getConfig().getValue("quarkus.rest-client.demoServiceExternal.perform", Boolean.class)) {
                    completionStages.put(3, externDemoService.get1(authHeaderContent).toCompletableFuture());
                    completionStages.put(4, externDemoService.get2(authHeaderContent).toCompletableFuture());
                }

                for (Map.Entry<Integer, CompletableFuture<String>> curStage : completionStages.entrySet()) {
                    CompletableFuture<String> future = curStage.getValue();

                    String result = null;
                    log.info("Waiting on call {}", curStage.getKey());
                    try {
                        result = future.get(ConfigProvider.getConfig().getValue("quarkus.rest-client.demoServiceExternal.readTimeout", Integer.class), TimeUnit.MILLISECONDS);
                    } catch (Throwable e) {
                        log.warn("Failed to make call {}: ", curStage.getKey(), e);
                        result = e.getClass().getName() + " - " + e.getMessage();
                    }
                    log.info("Got result from call {} - {}", curStage.getKey(), result);

                    switch (curStage.getKey()) {
                        case 1:
                            response1 = result;
                            break;
                        case 2:
                            response2 = result;
                            break;
                        case 3:
                            responseExt1 = result;
                            break;
                        case 4:
                            responseExt2 = result;
                            break;
                    }
                }
            }
            {
//                if(ConfigProvider.getConfig().getValue("quarkus.rest-client.demoService.perform", Boolean.class)) {
//                    try {
//                        response1 = demoService.get1(authHeaderContent);
//                    } catch (Throwable e) {
//                        log.warn("Failed to reach service for 1: ", e);
//                        response1 = e.getMessage();
//                    }
//                    try {
//                        response2 = demoService.get2(authHeaderContent);
//                    } catch (Throwable e) {
//                        log.warn("Failed to reach service for 2: ", e);
//                        response2 = e.getMessage();
//                    }
//                }
//                if(ConfigProvider.getConfig().getValue("quarkus.rest-client.demoServiceExternal.perform", Boolean.class)) {
//                    try {
//                        responseExt1 = externDemoService.get1(authHeaderContent);
//                    } catch (Throwable e) {
//                        log.warn("Failed to reach service for external 1: ", e);
//                        responseExt1 = e.getMessage();
//                    }
//                    try {
//                        responseExt2 = externDemoService.get2(authHeaderContent);
//                    } catch (Throwable e) {
//                        log.warn("Failed to reach service for external 2: ", e);
//                        responseExt2 = e.getMessage();
//                    }
//                }
            }
            log.info("Finished demo service calls: {}/{}/{}/{}", response1, response2, responseExt1, responseExt2);
        }

        Response.ResponseBuilder responseBuilder = Response.ok(
                overview
                        .data(USER_INFO_DATA_KEY, UserGetResponse.builder(user).build())
                        .data("numItems", inventoryItemService.count())
                        .data("numStorageBlocks", storageBlockService.count())
                        .data("response1", response1)
                        .data("response2", response2)
                        .data("responseExt1", responseExt1)
                        .data("responseExt2", responseExt2),
                MediaType.TEXT_HTML_TYPE
        );

        if (newCookies != null && !newCookies.isEmpty()) {
            responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
        }

        return responseBuilder.build();
    }

}
