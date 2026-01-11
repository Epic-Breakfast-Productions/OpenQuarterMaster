package tech.ebp.oqm.lib.core.api.quarkus.runtime.dataHelpers;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

@Slf4j
public abstract class DataHelperService {
	
	public static boolean oidcSetup(){
		if(
			ConfigProvider.getConfig().getOptionalValue("quarkus.oidc.client-id", String.class).isEmpty() ||
			ConfigProvider.getConfig().getOptionalValue("quarkus.oidc.credentials.secret", String.class).isEmpty()
		){
			return false;
		}
		return true;
	}
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;
	
	@Inject
	KcClientAuthService serviceAccountService;
	
}
