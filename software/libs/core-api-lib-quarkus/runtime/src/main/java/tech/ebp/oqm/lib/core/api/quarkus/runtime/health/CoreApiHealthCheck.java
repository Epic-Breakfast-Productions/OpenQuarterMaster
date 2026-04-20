package tech.ebp.oqm.lib.core.api.quarkus.runtime.health;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

@Readiness
@ApplicationScoped
public class CoreApiHealthCheck implements AsyncHealthCheck {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@Override
	public Uni<HealthCheckResponse> call() {
		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("OqmCoreApi");
		return this.oqmCoreApiClient.getApiServerHealth()
				   .map((ObjectNode coreApiHealth) -> {
					   String status = coreApiHealth.get("status").asText();

					   if(status.equalsIgnoreCase(HealthCheckResponse.Status.UP.name())){
						   responseBuilder.up();
					   } else {
						   responseBuilder.down();
					   }
					   return responseBuilder.build();
				   })
				   .onFailure().recoverWithItem(e -> {
				return responseBuilder.down().withData("error", e.getClass().getName() + " - " + e.getMessage()).build();
			})
			;
	}
}
