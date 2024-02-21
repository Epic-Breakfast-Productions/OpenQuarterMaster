package com.ebp.openQuarterMaster.plugin.interfaces.rest;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.ItemSearchResults;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.service.ItemSearchService;
import com.ebp.openQuarterMaster.plugin.restClients.searchObj.InventoryItemSearch;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("/api/v1/itemSearch")
@RequestScoped
@Tags({@Tag(name = "Item Search", description = "Endpoints for searching for items")})
public class ItemSearch {
    
    @Inject
    ItemSearchService searchService;
    
    @GET
    @RolesAllowed("inventoryView")
    @Produces(MediaType.APPLICATION_JSON)
    public ItemSearchResults identifyModule(
        @BeanParam InventoryItemSearch inventoryItemSearch
    ) {
        return this.searchService.searchForItemLocations(inventoryItemSearch, true);
    }
}
