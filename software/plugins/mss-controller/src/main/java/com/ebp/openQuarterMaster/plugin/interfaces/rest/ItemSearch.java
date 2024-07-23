package com.ebp.openQuarterMaster.plugin.interfaces.rest;

import com.ebp.openQuarterMaster.plugin.interfaces.RestInterface;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.ItemSearchResults;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.service.ItemSearchService;
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
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;

@Path("/api/v1/itemSearch")
@RequestScoped
@Tags({@Tag(name = "Item Search", description = "Endpoints for searching for items")})
public class ItemSearch extends RestInterface {
    
    @Inject
    ItemSearchService searchService;
    
    @GET
    @RolesAllowed("inventoryView")
    @Produces(MediaType.APPLICATION_JSON)
    public ItemSearchResults identifyModule(
        @BeanParam InventoryItemSearch inventoryItemSearch
    ) {
        return this.searchService.searchForItemLocations(this.getSelectedDb(), inventoryItemSearch, true);
    }
}
