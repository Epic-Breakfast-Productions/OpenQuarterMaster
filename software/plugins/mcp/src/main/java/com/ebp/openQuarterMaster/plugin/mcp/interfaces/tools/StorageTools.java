package com.ebp.openQuarterMaster.plugin.mcp.interfaces.tools;

import com.ebp.openQuarterMaster.plugin.mcp.interfaces.McpTool;
import io.quarkiverse.mcp.server.Tool;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

@RequestScoped
public class StorageTools extends McpTool {


	@Inject
	KcClientAuthService serviceAccountService;

	@Tool(description = "Get number of storage blocks.")
	public long getNumStorageBlocks() {
		return this.getOqmCoreApiClientService()
				   .storageBlockCollectionStats(
					   serviceAccountService.getAuthString(),
					   "default"
				   ).await()
				   .indefinitely()
				   .get("size").asLong();
	}
}
