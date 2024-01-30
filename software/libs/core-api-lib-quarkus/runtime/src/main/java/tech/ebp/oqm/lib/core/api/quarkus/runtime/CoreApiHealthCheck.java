package tech.ebp.oqm.lib.core.api.quarkus.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Readiness
@ApplicationScoped
public class CoreApiHealthCheck implements HealthCheck {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@Override
	public HealthCheckResponse call() {
		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("OqmCoreApi");
		
		try {
			JsonObject returned = this.oqmCoreApiClient.getApiServerHealth();
			String status = returned.getString("status");
			
			if(status.equalsIgnoreCase(HealthCheckResponse.Status.UP.name())){
				responseBuilder.up();
			} else {
				responseBuilder.down();
			}
		} catch (Exception e) {
			responseBuilder.down().withData("error", e.getMessage());
		}
		
		return responseBuilder.build();
	}
}
