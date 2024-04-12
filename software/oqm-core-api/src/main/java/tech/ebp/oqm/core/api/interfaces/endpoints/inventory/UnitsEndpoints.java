package tech.ebp.oqm.core.api.interfaces.endpoints.inventory;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.model.rest.unit.custom.NewCustomUnitRequest;
import tech.ebp.oqm.core.api.model.rest.unit.custom.NewDerivedCustomUnitRequest;
import tech.ebp.oqm.core.api.model.units.CustomUnitEntry;
import tech.ebp.oqm.core.api.model.units.UnitCategory;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.model.units.ValidUnitDimension;
import tech.ebp.oqm.core.api.rest.search.CustomUnitSearch;
import tech.ebp.oqm.core.api.service.mongo.CustomUnitService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;

import javax.measure.Unit;
import java.util.Map;
import java.util.Set;

@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/unit")
@Tags({@Tag(name = "Units", description = "Endpoints for managing Units.")})
@RequestScoped
public class UnitsEndpoints extends EndpointProvider {

    @Inject
    CustomUnitService customUnitService;

    @GET
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
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Map<UnitCategory, Set<Unit<?>>> getUnits() {
        log.info("Getting valid unit list.");
        return UnitUtils.UNIT_CATEGORY_MAP;
    }

    @GET
    @Path("dimensions")
    @Operation(
            summary = "Gets the list of supported unit dimensions."
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
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public ValidUnitDimension[] getUnitDimensions() {
        log.info("Getting valid unit list.");
        return ValidUnitDimension.values();
    }

    @GET
    @Path("deriveTypes")
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
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public NewDerivedCustomUnitRequest.DeriveType[] getUnitDeriveTypes() {
        log.info("Getting valid unit list.");
        return NewDerivedCustomUnitRequest.DeriveType.values();
    }

    @GET
    @Path("compatibility")
    @Operation(
            summary = "Gets the set of compatible units."
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
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Unit<?>, Set<Unit<?>>> getUnitCompatibleMap() {
        log.info("Getting unit set with lists of compatible units.");
        return UnitUtils.UNIT_COMPATIBILITY_MAP;
    }

    @GET
    @Path("compatibility/{unit}")
    @Operation(
            summary = "Gets the compatible units of the unit given"
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
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnitCompatible(
            @PathParam("unit") String unitString
    ) {
        log.info("Getting unit set with lists of compatible units.");
        Unit<?> unit;
        try {
            unit = UnitUtils.unitFromString(unitString);
        } catch (IllegalArgumentException e) {
            //TODO:: determine proper return code
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .entity("Invalid Unit String given: \"" + unitString + "\"")
                    .build();
        }
        Set<Unit<?>> units = UnitUtils.UNIT_COMPATIBILITY_MAP.get(unit);
        return Response.ok(units).build();

    }

    @POST
    @Path("custom")
    @Operation(
            summary = "Adds a new custom unit."
    )
    @APIResponse(
            responseCode = "200",
            description = "Object added.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = ObjectId.class
                    )
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad request given. Data given could not pass validation.",
            content = @Content(mediaType = "text/plain")
    )
    @RolesAllowed(Roles.INVENTORY_ADMIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectId createCustomUnit(
            @Valid NewCustomUnitRequest ncur
    ) {
        return this.customUnitService.add(ncur);
    }

    @GET
    @Path("custom")
    @Operation(
            summary = "Gets all custom units."
    )
    @APIResponse(
            responseCode = "200",
            description = "Object added.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = ObjectId.class
                    )
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad request given. Data given could not pass validation.",
            content = @Content(mediaType = "text/plain")
    )
    @RolesAllowed(Roles.INVENTORY_ADMIN)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult<CustomUnitEntry>
    getCustomUnits(
            @BeanParam CustomUnitSearch search
    ) {
        return this.customUnitService.search(search);
    }
}
