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
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

@RequestScoped
public class DatabaseTools extends McpTool {
	public static final String TN_LIST_DATABASES = "listDatabases";

	@Tool(
		title = "List databases.",
		name = TN_LIST_DATABASES,
		description = "Get a list of databases available to use."
	)
	public Uni<ArrayNode> listDatabases() {
		return this.getOqmCoreApiClientService()
				   .manageDbList(getAuthString())
				   .invoke(dbList->{
					   for(JsonNode curDb : dbList){
						   ObjectNode curDbObj = (ObjectNode) curDb;
						   curDbObj.remove("attributes");
						   curDbObj.remove("keywords");
						   curDbObj.remove("usersAllowed");
						   curDbObj.remove("schemaVersion");
					   }
				   });
	}

}
