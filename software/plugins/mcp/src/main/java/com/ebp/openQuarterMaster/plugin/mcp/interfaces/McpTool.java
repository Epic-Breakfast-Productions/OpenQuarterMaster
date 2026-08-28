package com.ebp.openQuarterMaster.plugin.mcp.interfaces;

import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

@Slf4j
public abstract class McpTool {

	@Getter
	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;
}
