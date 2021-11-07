package com.ebp.openQuarterMaster.baseStation.endpoints.info;

import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.measure.Unit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.Map;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

@Traced
@Slf4j
@Path("/info")
@Tags({@Tag(name = "Informational", description = "Endpoints for getting general information from the server.")})
@ApplicationScoped
public class GeneralInfo extends EndpointProvider {

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
                            implementation = java.util.Map.class
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
