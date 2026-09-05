package com.ebp.openQuarterMaster.plugin.mcp.interfaces.tools;

import com.ebp.openQuarterMaster.plugin.mcp.interfaces.McpTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

import java.util.List;

@RequestScoped
public class InvItemTools extends McpTool {
	public static final String TN_GET_NUM_ITEMS = "getNumInventoryItems";
	public static final String TN_GET_ITEM_DETAILS = "getNumInventoryItemDetails";
	public static final String TN_SEARCH_ITEMS_BY_NAME = "searchInventoryItemsByName";


	@Tool(
		title = "Get number of inventory items.",
		name = TN_GET_NUM_ITEMS,
		description = "Get number of inventory items present in the database."
	)
	public Uni<String> getNumItems(
		@ToolArg(description = "The database to use.") String dbName
	) {
		return this.getOqmCoreApiClientService()
				   .invItemCollectionStats(
					   getAuthString(),
					   dbName
				   )
				   .map(s->s.get("size").asText());
	}

	@Tool(
		title = "Search for inventory items by name.",
		name = TN_SEARCH_ITEMS_BY_NAME,
		description = "Search inventory items present in the database by name."
	)
	public Uni<ArrayNode> searchItemsByName(
		@ToolArg(description = "The database to use.") String dbName,
		@ToolArg(description = "The name, full or partial, of the item(s) to search for.") String name
	) {
		return this.getOqmCoreApiClientService()
				   .invItemSearch(
					   getAuthString(),
					   dbName,
					   InventoryItemSearch.builder()
						   .name(name)
						   .build()
				   )
				   .map(s->{
					   ArrayNode output = getMapper().createArrayNode();

					   for(JsonNode curResult : s.get("results")){
						   output.add(
							   getMapper().createObjectNode()
								   .put("id", curResult.get("id").asText())
								   .put("name", curResult.get("name").asText())
						   );
					   }

					   return output;
				   });
	}

	@Tool(
		title = "Get inventory item details.",
		name = TN_GET_ITEM_DETAILS,
		description = "Get an inventory item's details."
	)
	public Uni<ObjectNode> getItemDetails(
		@ToolArg(description = "The database to use.") String dbName,
		@ToolArg(description = "The ID of the item being gotten.") String itemId
	) {
		return this.getOqmCoreApiClientService()
				   .invItemGet(
					   getAuthString(),
					   dbName,
					   itemId
				   );
	}
}
