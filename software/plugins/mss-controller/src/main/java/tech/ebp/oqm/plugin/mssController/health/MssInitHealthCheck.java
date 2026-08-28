package tech.ebp.oqm.plugin.mssController.health;

import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Startup;
import tech.ebp.oqm.plugin.mssController.service.mssConn.MssConnectionService;

@Startup
public class MssInitHealthCheck implements HealthCheck {
	private static final String NAME = "mss-init-health-check";

	@Inject
	MssConnectionService mssConnectionService;

	@Override
	public HealthCheckResponse call() {
		HealthCheckResponseBuilder builder = HealthCheckResponse.named(NAME);

		if(this.mssConnectionService.isSetUp()){
			builder.down();
		} else {
			builder.up();
		}

		return builder.build();
	}

}
