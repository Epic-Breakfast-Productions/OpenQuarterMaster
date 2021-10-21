package com.ebp.openQuarterMaster.baseStation.endpoints.inventory.items;

import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.lib.core.InventoryItem;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/inventory/item")
public class InventoryItemsCrud {
    @Inject
    InventoryItemService service;

    @POST
    @Operation(
            summary = "Adds a new inventory item."
    )
    @Tags({@Tag(name = "Inventory Items")})
    @APIResponse(
            responseCode = "200",
            description = "Got the user's info.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = InventoryItem.class
                    )
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad request given. Data given could not pass validation. (no user at given id, etc.)",
            content = @Content(mediaType = "text/plain")
    )
    @Produces(MediaType.APPLICATION_JSON    )
    public ObjectId createInventoryItem(@Valid InventoryItem item) {
        return service.add(item);
    }
}
