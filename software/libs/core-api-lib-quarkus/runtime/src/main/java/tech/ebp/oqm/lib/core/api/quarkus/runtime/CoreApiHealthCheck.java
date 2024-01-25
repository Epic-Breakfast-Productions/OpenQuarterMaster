package tech.ebp.oqm.lib.core.api.quarkus.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class CoreApiHealthCheck implements HealthCheck {
	
	@Override
	public HealthCheckResponse call() {
		//TODO:: call core API health, base on that
		return HealthCheckResponse.named("OqmCoreApi").up().build();
	}
}
