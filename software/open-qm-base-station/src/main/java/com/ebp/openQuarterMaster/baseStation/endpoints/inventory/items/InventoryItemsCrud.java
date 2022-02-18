package com.ebp.openQuarterMaster.baseStation.endpoints.inventory.items;

import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SortType;
import com.ebp.openQuarterMaster.lib.core.storage.items.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Traced
@Slf4j
@Path("/api/inventory/item")
@Tags({@Tag(name = "Inventory Items", description = "Endpoints for inventory item CRUD, and managing stored items.")})
@RequestScoped
public class InventoryItemsCrud extends EndpointProvider {

    @Inject
    InventoryItemService inventoryItemService;

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Operation(
            summary = "Adds a new inventory item."
    )
    @APIResponse(
            responseCode = "201",
            description = "Item added.",
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
            @Valid InventoryItem item
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Creating new item.");
        User user = this.userService.getFromJwt(jwt);

        ObjectId output = inventoryItemService.add(item, user);
        log.info("Item created with id: {}", output);
        return Response.status(Response.Status.CREATED).entity(output).build();
    }


    @GET
    @Operation(
            summary = "Gets a list of inventory items."
    )
    @APIResponse(
            responseCode = "200",
            description = "Items retrieved.",
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
    @RolesAllowed("user")
    @Produces({MediaType.APPLICATION_JSON})
    public Response listInventoryItems(
            @Context SecurityContext securityContext,
            //for actual queries
            @QueryParam("name") String name,
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

        Bson sort = SearchUtils.getSortBson(sortField, sortType);
        PagingOptions pageOptions = PagingOptions.fromQueryParams(pageSize, pageNum, false);

        SearchResult<InventoryItem> searchResult = this.inventoryItemService.search(
                name,
                keywords,
                storedType,
                sort,
                pageOptions
        );

        if (searchResult.getResults().isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response
                .status(Response.Status.OK)
                .entity(searchResult.getResults())
                .header("num-elements", searchResult.getResults().size())
                .header("query-num-results", searchResult.getNumResultsForEntireQuery())
                .build();
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Gets a particular inventory item."
    )
    @APIResponse(
            responseCode = "200",
            description = "Item retrieved.",
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
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInventoryItem(
            @Context SecurityContext securityContext,
            @PathParam String id
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Retrieving item with id {}", id);
        InventoryItem output = inventoryItemService.get(id);

        if (output == null) {
            log.info("Item not found.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("Item found");
        return Response.status(Response.Status.FOUND).entity(output).build();
    }

    @PUT
    @Path("{id}")
    @Operation(
            summary = "Updates a particular inventory item.",
            description = "Partial update to an inventory item. Do not need to supply all fields, just the one you wish to update."
    )
    @APIResponse(
            responseCode = "200",
            description = "Item updated.",
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
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateInventoryItem(
            @Context SecurityContext securityContext,
            @PathParam String id,
            ObjectNode itemUpdates
    ) {
        logRequestContext(this.jwt, securityContext);
        log.info("Updating item with id {}", id);
        User user = this.userService.getFromJwt(jwt);

        InventoryItem updated = this.inventoryItemService.update(id, itemUpdates, user);

        return Response.ok(updated).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Deletes a particular inventory item."
    )
    @APIResponse(
            responseCode = "200",
            description = "Item deleted.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = InventoryItem.class
                    )
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "No item found to delete.",
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
        InventoryItem output = inventoryItemService.remove(id, user);

        if (output == null) {
            log.info("Item not found.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("Item found, deleted.");
        return Response.status(Response.Status.OK).entity(output).build();
    }

    @GET
    @Path("{itemId}/{storageBlockId}")
    @Operation(
            summary = "Gets the stored amount or tracked item to the storage block specified."
    )
    @APIResponse(
            responseCode = "200",
            description = "Item added.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = InventoryItem.class
                    )
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "No item found to delete.",
            content = @Content(mediaType = "text/plain")
    )
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStoredInventoryItem(
            @Context SecurityContext securityContext,
            @PathParam String itemId,
            @PathParam String storageBlockId
    ) {
        logRequestContext(this.jwt, securityContext);
        //TODO
        return Response.serverError().entity("Not implemented yet.").build();
    }

    @PUT
    @Path("{itemId}/{storageBlockId}")
    @Operation(
            summary = "Adds a stored amount or tracked item to the storage block specified."
    )
    @APIResponse(
            responseCode = "200",
            description = "Item added.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = InventoryItem.class
                    )
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "No item found to delete.",
            content = @Content(mediaType = "text/plain")
    )
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response addStoredInventoryItem(
            @Context SecurityContext securityContext,
            @PathParam String itemId,
            @PathParam String storageBlockId
    ) {
        logRequestContext(this.jwt, securityContext);
        User user = this.userService.getFromJwt(jwt);
        //TODO
        return Response.serverError().entity("Not implemented yet.").build();
    }

    @DELETE
    @Path("{itemId}/{storageBlockId}")
    @Operation(
            summary = "Removes a stored amount or tracked item from the storage block specified."
    )
    @APIResponse(
            responseCode = "200",
            description = "Item added.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = InventoryItem.class
                    )
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "No item found to delete.",
            content = @Content(mediaType = "text/plain")
    )
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response removeStoredInventoryItem(
            @Context SecurityContext securityContext,
            @PathParam String itemId,
            @PathParam String storageBlockId
    ) {
        logRequestContext(this.jwt, securityContext);
        User user = this.userService.getFromJwt(jwt);
        //TODO
        return Response.serverError().entity("Not implemented yet.").build();
    }
}
