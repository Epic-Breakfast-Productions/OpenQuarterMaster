package com.ebp.openQuarterMaster.plugin.mcp.interfaces.tools;

import com.ebp.openQuarterMaster.plugin.mcp.interfaces.McpTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
	public static final String TN_SEARCH_ITEMS_BY_NAME = "searchInventoryItemsByName";

	private final ObjectMapper mapper = new ObjectMapper();

	@Inject
	KcClientAuthService serviceAccountService;

	@Tool(
		title = "Get number of inventory items.",
		name = TN_GET_NUM_ITEMS,
		description = "Get number of inventory items present in the database."
	)
	public Uni<String> getNumItems() {
		return this.getOqmCoreApiClientService()
				   .invItemCollectionStats(
					   serviceAccountService.getAuthString(),
					   "default"
				   )
				   .map(s->s.get("size").asText());
	}

	@Tool(
		title = "Search for inventory items by name.",
		name = TN_SEARCH_ITEMS_BY_NAME,
		description = "Search inventory items present in the database by name."
	)
	public Uni<ArrayNode> searchItemsByName(@ToolArg(description = "The name, full or partial, of the item(s) to search for.") String name) {
		return this.getOqmCoreApiClientService()
				   .invItemSearch(
					   serviceAccountService.getAuthString(),
					   "default",
					   InventoryItemSearch.builder()
						   .name(name)
						   .build()
				   )
				   .map(s->{
					   ArrayNode output = mapper.createArrayNode();

					   for(JsonNode curResult : s.get("results")){
						   output.add(
							   mapper.createObjectNode()
								   .put("id", curResult.get("id").asText())
								   .put("name", curResult.get("name").asText())
						   );
					   }

					   return output;
				   });
	}


}
