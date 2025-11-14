package tech.ebp.oqm.core.api.health;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Startup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Startup
@ApplicationScoped
public class ConfigHealthCheck implements HealthCheck {
	private static final String HEALTH_CHECK_NAME = "Config Sanity health check";
	
	private Map<String, String> checkRunningInfoConfig(){
		Map<String, String> invalidConfigs = new HashMap<>();
		
		try {
			log.debug(
				"Built self-referencing url: {}",
				new URL(ConfigProvider.getConfig().getValue("runningInfo.baseUrl", String.class))
			);
		} catch(IllegalArgumentException|MalformedURLException e){
			log.error("Failed to build url to reference self: ", e);
			invalidConfigs.put("runningInfo.baseUrl", "Could not build a self-referencing url from config:: " + e.getMessage());
		}
		
		return invalidConfigs;
	}
	
	
	@Override
	public HealthCheckResponse call() {
		Map<String, String> invalidConfigs = new HashMap<>();
		
		invalidConfigs.putAll(checkRunningInfoConfig());
		
		if(invalidConfigs.isEmpty()) {
			return HealthCheckResponse.up(HEALTH_CHECK_NAME);
		}
		HealthCheckResponseBuilder builder = HealthCheckResponse.named(HEALTH_CHECK_NAME);
		
		builder.down();
		
		for(Map.Entry<String, String> curInv : invalidConfigs.entrySet()){
			builder.withData(curInv.getKey(), curInv.getValue());
		}
		
		return builder.build();
	}
}