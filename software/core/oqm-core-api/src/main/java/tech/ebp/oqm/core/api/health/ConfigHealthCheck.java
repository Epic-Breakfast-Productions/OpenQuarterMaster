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

/**
 * This left as a placeholder for future use.
 */
@Slf4j
@Startup
@ApplicationScoped
public class ConfigHealthCheck implements HealthCheck {
	
	private static final String HEALTH_CHECK_NAME = "Config Sanity health check";
	
	private Map<String, String> checkRunningInfoConfig() {
		Map<String, String> invalidConfigs = new HashMap<>();
		
		
		return invalidConfigs;
	}
	
	
	@Override
	public HealthCheckResponse call() {
		Map<String, String> invalidConfigs = new HashMap<>();
		
		invalidConfigs.putAll(checkRunningInfoConfig());
		
		if (invalidConfigs.isEmpty()) {
			return HealthCheckResponse.up(HEALTH_CHECK_NAME);
		}
		HealthCheckResponseBuilder builder = HealthCheckResponse.named(HEALTH_CHECK_NAME);
		
		builder.down();
		
		for (Map.Entry<String, String> curInv : invalidConfigs.entrySet()) {
			builder.withData(curInv.getKey(), curInv.getValue());
		}
		
		return builder.build();
	}
}