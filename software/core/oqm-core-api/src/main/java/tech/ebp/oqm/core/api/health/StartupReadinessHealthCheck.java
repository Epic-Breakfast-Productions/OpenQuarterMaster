package tech.ebp.oqm.core.api.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class StartupReadinessHealthCheck implements HealthCheck {

	private static final String HEALTH_CHECK_NAME = "custom startup initialization health check";

	@Inject
	Instance<StatusProvider> statusProviders;

	@Override
	public HealthCheckResponse call() {
		HealthCheckResponseBuilder builder = HealthCheckResponse.named(HEALTH_CHECK_NAME);
		boolean allReady = true;

		for (StatusProvider status : statusProviders) {
			boolean ready = status.isReady();
			allReady &= ready;
			builder.withData(status.getName() + ".ready", ready);
			builder.withData(status.getName() + ".status", status.getStatusMessage());
		}

		if (allReady) {
			builder.up();
		} else {
			builder.down();
		}

		return builder.build();
	}
}

