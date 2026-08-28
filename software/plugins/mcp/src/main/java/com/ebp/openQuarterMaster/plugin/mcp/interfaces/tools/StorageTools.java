package com.ebp.openQuarterMaster.plugin.mcp.interfaces.tools;

import com.ebp.openQuarterMaster.plugin.mcp.interfaces.McpTool;
import io.quarkiverse.mcp.server.Tool;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

@RequestScoped
public class StorageTools extends McpTool {
	public static final String TN_GET_NUM_STORAGE_BLOCKS = "getNumStorageBlocks";


	@Inject
	KcClientAuthService serviceAccountService;

	@Tool(
		title = "Get number of storage blocks.",
		name = TN_GET_NUM_STORAGE_BLOCKS,
		description = "Get number of storage blocks present in the database."
	)
	public String getNumStorageBlocks() {
		return this.getOqmCoreApiClientService()
				   .storageBlockCollectionStats(
					   serviceAccountService.getAuthString(),
					   "default"
				   ).await()
				   .indefinitely()
				   .get("size").asText();
	}
}
