package com.ebp.openQuarterMaster.baseStation.endpoints.inventory.items;

import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.SearchUtils;
import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

@Traced
@Slf4j
@Path("/inventory/item")
@Tags({@Tag(name = "Inventory Items")})
public class InventoryItemsCrud {

    @Inject
    InventoryItemService service;


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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createInventoryItem(@Valid InventoryItem item) {
        log.info("Creating new item.");
        ObjectId output = service.add(item);
        log.info("Item created with id: {}", output);
        return Response.status(Response.Status.CREATED).entity(output).build();
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
            description = "Bad request given. Data given could not pass validation.)",
            content = @Content(mediaType = "text/plain")
    )
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInventoryItem(@PathParam String id) {
        log.info("Retrieving item with id {}", id);
        InventoryItem output = service.get(id);

        if(output == null){
            log.info("Item not found.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("Item found");
        return Response.status(Response.Status.FOUND).entity(output).build();
    }

    @GET
    @Operation(
            summary = "Gets a list inventory items."
    )
    @APIResponse(
            responseCode = "200",
            description = "Item retrieved.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            type = SchemaType.ARRAY,
                            implementation = InventoryItem.class
                    )
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "No items found from query given.",
            content = @Content(mediaType = "text/plain")
    )
    @Produces(MediaType.APPLICATION_JSON)
    public Response listInventoryItems(
            @QueryParam("name") String name
    ) {
        log.info("Searching for items with: ");

        List<Bson> filters = new ArrayList<>();

        if(name != null && !name.isBlank()){
            filters.add(regex("name", SearchUtils.getSearchTermPattern(name)));
        }

        List<InventoryItem> output = this.service.list(and(filters));

        if(output.isEmpty()){
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response.status(Response.Status.FOUND).entity(output).build();
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteInventoryItem(@PathParam String id) {
        log.info("Deleting item with id {}", id);
        InventoryItem output = service.remove(id);

        if(output == null){
            log.info("Item not found.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("Item found, deleted.");
        return Response.status(Response.Status.OK).entity(output).build();
    }
}
