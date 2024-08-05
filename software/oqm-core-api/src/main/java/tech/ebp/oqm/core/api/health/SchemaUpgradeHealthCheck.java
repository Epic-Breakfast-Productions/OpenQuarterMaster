package tech.ebp.oqm.core.api.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import tech.ebp.oqm.core.api.service.schemaVersioning.ObjectSchemaUpgradeService;

@Readiness
@ApplicationScoped
public class SchemaUpgradeHealthCheck implements HealthCheck {

	@Inject
	ObjectSchemaUpgradeService objectSchemaUpgradeService;

	@Override
	public HealthCheckResponse call() {
		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.builder()
			.name("Database Schema Upgrade");

		if(objectSchemaUpgradeService.upgradeRan()){
			responseBuilder = responseBuilder.up();
		} else {
			responseBuilder = responseBuilder.down();
		}

		return responseBuilder.build();
	}
}
