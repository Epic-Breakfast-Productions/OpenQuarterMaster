package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.health;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.config.OqmCoreCharacteristicsConfig;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.OqmCoreCharacteristicsRestClient;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.service.OqmCoreCharacteristicsService;

@Readiness
@ApplicationScoped
public class CoreCharacteristicsHealthCheck implements AsyncHealthCheck {

	@RestClient
	OqmCoreCharacteristicsRestClient oqmCoreCharacteristicsClient;

	@Inject
	OqmCoreCharacteristicsService oqmCoreCharacteristicsService;

	@Override
	public Uni<HealthCheckResponse> call() {
		if (!this.oqmCoreCharacteristicsService.isEnabled()) {
			return Uni.createFrom().item(HealthCheckResponse.named("OqmCoreCharacteristics").up().withData("enabled", false).build());
		}

		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("OqmCoreCharacteristics");
		responseBuilder.withData("enabled", true);
		return this.oqmCoreCharacteristicsClient.health()
				   .map((ObjectNode coreCharacteristicsHealth)->{
					   String status = coreCharacteristicsHealth.get("status").asText();

					   if (status.equalsIgnoreCase(HealthCheckResponse.Status.UP.name())) {
						   responseBuilder.up();
					   } else {
						   responseBuilder.down();
					   }
					   return responseBuilder.build();
				   })
				   .onFailure().recoverWithItem(e->{
				return responseBuilder.down().withData("error", e.getClass().getName() + " - " + e.getMessage()).build();
			})
			;
	}
}
