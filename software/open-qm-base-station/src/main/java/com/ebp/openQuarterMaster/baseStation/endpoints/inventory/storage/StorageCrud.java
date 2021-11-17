package com.ebp.openQuarterMaster.baseStation.endpoints.inventory.storage;

import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SortType;
import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.StorageBlock;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Traced
@Slf4j
@Path("/api/storage")
@Tags({@Tag(name = "Storage", description = "Endpoints for managing Storage Mediums.")})
@RequestScoped
public class StorageCrud extends EndpointProvider {
    @Inject
    StorageBlockService storageBlockService;

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Operation(
            summary = "Adds a new storage block."
    )
    @APIResponse(
            responseCode = "201",
            description = "Storage Block added.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = ObjectId.class
                    )
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad request given. Data given could not pass validation.)",
            content = @Content(mediaType = "text/plain")
    )
    @RolesAllowed("user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createInventoryItem(
            @Context SecurityContext securityContext,
            @Valid StorageBlock storageBlock
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Creating new storage block.");
        User user = this.userService.getFromJwt(jwt);

        //TODO:: make validator, extract
        if (storageBlock.getParent() != null) {
            StorageBlock parent = this.storageBlockService.get(storageBlock.getParent());
            if (parent == null) {
                throw new IllegalArgumentException("No parent exists for storage block.");
            }
        }

        ObjectId output = storageBlockService.add(storageBlock, user);
        log.info("Storage block created with id: {}", output);
        return Response.status(Response.Status.CREATED).entity(output).build();
    }


    @GET
    @Operation(
            summary = "Gets a list of storage blocks."
    )
    @APIResponse(
            responseCode = "200",
            description = "Blocks retrieved.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            type = SchemaType.ARRAY,
                            implementation = InventoryItem.class
                    )
            ),
            headers = {
                    @Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
                    @Header(name = "query-num-results", description = "Gives the number of results in the query given.")
            }
    )
    @APIResponse(
            responseCode = "204",
            description = "No items found from query given.",
            content = @Content(mediaType = "text/plain")
    )
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed("user")
    public Response listInventoryItems(
            @Context SecurityContext securityContext,
            //for actual queries
            @QueryParam("label") String label,
            @QueryParam("location") String location,
            @QueryParam("parents") List<String> parents,
            @QueryParam("keywords") List<String> keywords,
            @QueryParam("storedType") StoredType storedType,
            //paging
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("pageNum") Integer pageNum,
            //sorting
            @QueryParam("sortBy") String sortField,
            @QueryParam("sortType") SortType sortType
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Searching for storage blocks with: ");

        Bson sort = SearchUtils.getSortBson(sortField, sortType);
        PagingOptions pageOptions = PagingOptions.fromQueryParams(pageSize, pageNum, false);

        SearchResult<StorageBlock> output = this.storageBlockService.search(
                label,
                location,
                parents,
                keywords,
                sort,
                pageOptions
        );

        if (output.getResults().isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response
                .status(Response.Status.OK)
                .entity(output.getResults())
                .header("num-elements", output.getResults().size())
                .header("query-num-results", output.getNumResultsForEntireQuery())
                .build();
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Gets a particular storage block."
    )
    @APIResponse(
            responseCode = "200",
            description = "Block retrieved.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = InventoryItem.class
                    )
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad request given. Data given could not pass validation.",
            content = @Content(mediaType = "text/plain")
    )
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response getStorageBlock(
            @Context SecurityContext securityContext,
            @PathParam String id
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Retrieving storage block with id {}", id);
        StorageBlock output = storageBlockService.get(id);

        if (output == null) {
            log.info("Storage block not found.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("Storage block found");
        return Response.status(Response.Status.FOUND).entity(output).build();
    }

    @PUT
    @Path("{id}")
    @Operation(
            summary = "Updates a particular storage block.",
            description = "Partial update to a storage block. Do not need to supply all fields, just the one you wish to update."
    )
    @APIResponse(
            responseCode = "200",
            description = "Storage block updated.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = StorageBlock.class
                    )
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad request given. Data given could not pass validation.",
            content = @Content(mediaType = "text/plain")
    )
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateInventoryItem(
            @Context SecurityContext securityContext,
            @PathParam String id,
            ObjectNode storageBlockUpdates
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Updating storage block with id {}", id);
        User user = this.userService.getFromJwt(jwt);

        StorageBlock updated = this.storageBlockService.update(id, storageBlockUpdates, user);

        return Response.ok(updated).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Deletes a particular Storage block."
    )
    @APIResponse(
            responseCode = "200",
            description = "Storage block deleted.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = StorageBlock.class
                    )
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "No Storage Block found to delete.",
            content = @Content(mediaType = "text/plain")
    )
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteInventoryItem(
            @Context SecurityContext securityContext,
            @PathParam String id
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Deleting item with id {}", id);
        User user = this.userService.getFromJwt(jwt);
        StorageBlock output = storageBlockService.remove(id, user);

        if (output == null) {
            log.info("Storage block not found.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("Storage block found, deleted.");
        return Response.status(Response.Status.OK).entity(output).build();
    }
}
