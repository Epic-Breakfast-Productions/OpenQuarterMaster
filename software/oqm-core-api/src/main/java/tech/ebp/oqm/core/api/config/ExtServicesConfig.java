package tech.ebp.oqm.core.api.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Map;

@ConfigMapping(prefix = "externalService")
public interface ExtServicesConfig {
	
	@WithName("secretSizeMin")
	int secretSizeMin();
	
	@WithName("secretSizeMax")
	int secretSizeMax();
	
	@WithName("serviceTokenExpires")
	long serviceTokenExpires();
	
	@WithName("extServices")
	Map<String, ExtServiceConfig> extServices();
	
	interface ExtServiceConfig{
		String secret();
	}
}
