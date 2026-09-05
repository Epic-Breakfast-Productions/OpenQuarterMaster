package com.ebp.openQuarterMaster.plugin.mcp.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

@Slf4j
public abstract class McpTool {

	@Getter
	private final ObjectMapper mapper = new ObjectMapper();

	@Getter
	@Inject
	KcClientAuthService serviceAccountService;

	@Getter
	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;
	
	protected String getAuthString(){
		return serviceAccountService.getAuthString();
	}
}
