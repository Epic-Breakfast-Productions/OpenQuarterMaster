package tech.ebp.oqm.plugin.alertMessenger.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Startup;

@Startup
@ApplicationScoped
public class EncryptionKeyHealthCheck implements HealthCheck {
	@Inject
	EncryptionKeyState state;

	@Override
	public HealthCheckResponse call() {
		HealthCheckResponseBuilder builder = HealthCheckResponse.named("Encryption key");
		return state.isValid() ? builder.up().build() : builder.down().withData("reason", state.getReason()).build();
	}
}