package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/coreApiHealth")
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ApiHealthCheckPassthrough extends PassthroughProvider {
	
	@GET
	public Uni<Response> getApiHealth() {
		return this.handleCall(
			this.getOqmCoreApiClient()
				   .getApiServerHealth()
		);
	}
	
}
