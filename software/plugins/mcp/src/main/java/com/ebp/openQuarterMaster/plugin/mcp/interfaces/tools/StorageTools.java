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
public class StorageTools extends McpTool {
	public static final String TN_GET_NUM_STORAGE_BLOCKS = "getNumStorageBlocks";
	public static final String TN_GET_STORAGE_BLOCK_DETAILS = "getStorageBlockDetails";
	public static final String TN_GET_SEARCH_STORAGE_BLOCKS_BY_NAME = "searchStorageBlocksByName";

	@Tool(
		title = "Get number of storage blocks.",
		name = TN_GET_NUM_STORAGE_BLOCKS,
		description = "Get number of storage blocks present in the database."
	)
	public Uni<String> getNumStorageBlocks(
		@ToolArg(description = "The database to use.") String dbName
	) {
		return this.getOqmCoreApiClientService()
				   .storageBlockCollectionStats(
					   getAuthString(),
					   dbName
				   )
				   .map(s->s.get("size").asText());
	}

	@Tool(
		title = "Search for storage blocks by name.",
		name = TN_GET_SEARCH_STORAGE_BLOCKS_BY_NAME,
		description = "Search storage blocks present in the database by name."
	)
	public Uni<ArrayNode> searchStorageBlocksByName(
		@ToolArg(description = "The database to use.") String dbName,
		@ToolArg(description = "The name") String name
	) {
		return this.getOqmCoreApiClientService()
				   .storageBlockSearch(
					   getAuthString(),
					   dbName,
					   StorageBlockSearch.builder()
						   .labelOrNickname(name)
						   .build()
				   )
				   .map(s->{
					   ArrayNode output = getMapper().createArrayNode();

					   for(JsonNode curResult : s.get("results")){
						   output.add(
							   getMapper().createObjectNode()
								   .put("id", curResult.get("id").asText())
								   .put("name", curResult.get("name").asText())
								   .put("label", curResult.get("label").asText())
						   );
					   }

					   return output;
				   });
	}


	@Tool(
		title = "Get storage block details.",
		name = TN_GET_STORAGE_BLOCK_DETAILS,
		description = "Get a storage block's details."
	)
	public Uni<ObjectNode> getBlockDetails(
		@ToolArg(description = "The database to use.") String dbName,
		@ToolArg(description = "The ID of the storage block being gotten.") String blockId
	) {
		return this.getOqmCoreApiClientService()
				   .storageBlockGet(
					   getAuthString(),
					   dbName,
					   blockId
				   );
	}
}
