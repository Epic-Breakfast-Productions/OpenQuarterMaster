package tech.ebp.oqm.plugin.mssController.interfaces.rest;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;
import tech.ebp.oqm.plugin.mssController.model.rest.HighlightedModule;

@Path("/module/highlight/")
public class ModuleHighlight {

	@Path("by/item")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<HighlightedModule> highlightByItem(InventoryItemSearch search) {

		//TODO:: search items
		//TODO:: from items, get storage blocks
		//TODO::search by storage blocks
		//TODO:: highlight blocks
		//TODO:: build and return response

		return Uni.createFrom().item(new HighlightedModule());
	}

	@Path("by/block")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<HighlightedModule> highlightByBlock(StorageBlockSearch search) {

		//TODO:: search items
		//TODO:: from items, get storage blocks
		//TODO::search by storage blocks
		//TODO:: highlight blocks
		//TODO:: build and return response

		return Uni.createFrom().item(new HighlightedModule());
	}
}
