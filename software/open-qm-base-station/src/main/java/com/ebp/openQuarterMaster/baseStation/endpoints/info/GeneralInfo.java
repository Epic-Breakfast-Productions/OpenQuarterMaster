package com.ebp.openQuarterMaster.baseStation.endpoints.info;

import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Map;

@Traced
@Slf4j
@Path("/api/info")
@Tags({@Tag(name = "Informational", description = "Endpoints for getting general information from the server.")})
@ApplicationScoped
public class GeneralInfo extends EndpointProvider {
//
//    @Inject
//    ServerInfoBean infoBean;
//
//    @GET
//    @Path("server")
//    @Operation(
//            summary = "Gets a set of information about the server."
//    )
//    @APIResponse(
//            responseCode = "200",
//            description = "Got the list.",
//            content = @Content(
//                    mediaType = "application/json",
//                    schema = @Schema(
//                            implementation = ServerInfoBean.class
//                    )
//            )
//    )
////    @SecurityRequirement(name = "JwtAuth")
//    @PermitAll
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getServerInfo(@Context SecurityContext ctx) {
//        log.info("Getting server info.");
//
//        return Response.ok(
//                this.infoBean
//        ).build();
//    }

    @GET
    @Path("units")
    @Operation(
            summary = "Gets the list of supported units."
    )
    @APIResponse(
            responseCode = "200",
            description = "Got the list.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            //TODO: better
                            implementation = Map.class
                    )
            )
    )
//    @SecurityRequirement(name = "JwtAuth")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnits(@Context SecurityContext ctx) {
        log.info("Getting valid unit list.");
        return Response.ok(
                UnitUtils.ALLOWED_UNITS_MAP
        ).build();
    }

}
