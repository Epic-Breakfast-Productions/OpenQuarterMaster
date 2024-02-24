package tech.ebp.oqm.lib.core.api.quarkus.runtime;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

@Readiness
@ApplicationScoped
public class CoreApiHealthCheck implements HealthCheck {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@Override
	public HealthCheckResponse call() {
		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("OqmCoreApi");
		
		try {
			ObjectNode returned = this.oqmCoreApiClient.getApiServerHealth().await().indefinitely();
			String status = returned.get("status").asText();
			
			if(status.equalsIgnoreCase(HealthCheckResponse.Status.UP.name())){
				responseBuilder.up();
			} else {
				responseBuilder.down();
			}
		} catch (Exception e) {
			responseBuilder.down().withData("error", e.getMessage());
		}
		
		return responseBuilder.build();
		
		//fails with "java.lang.IllegalStateException - The current thread cannot be blocked: vert.x-eventloop-thread-0"
		// https://stackoverflow.com/questions/78051079/issue-with-asynchealthcheck-in-extension
//		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("OqmCoreApi");
//		return this.oqmCoreApiClient.getApiServerHealth()
//				   .map((ObjectNode coreApiHealth) -> {
//					   ObjectNode returned = this.oqmCoreApiClient.getApiServerHealth().await().indefinitely();
//					   String status = returned.get("status").asText();
//
//					   if(status.equalsIgnoreCase(HealthCheckResponse.Status.UP.name())){
//						   responseBuilder.up();
//					   } else {
//						   responseBuilder.down();
//					   }
//					   return responseBuilder.build();
//				   })
//				   .onFailure().recoverWithItem(e -> {
//				return responseBuilder.down().withData("error", e.getClass().getName() + " - " + e.getMessage()).build();
//			})
//			;
	}
}
