package com.ebp.openQuarterMaster.plugin.restClients;

import com.ebp.openQuarterMaster.plugin.interfaces.rest.ItemSearch;
import com.ebp.openQuarterMaster.plugin.restClients.headerFactories.BaseStationAuthHeaderFactory;
import com.ebp.openQuarterMaster.plugin.restClients.searchObj.InventoryItemSearch;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/inventory/item")
@RegisterRestClient
@RegisterClientHeaders(BaseStationAuthHeaderFactory.class)
public interface BaseStationInventoryItemRestClient {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayNode searchItems(
		@BeanParam InventoryItemSearch search
	);
}
