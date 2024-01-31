package tech.ebp.oqm.lib.core.api.quarkus.runtime;

import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@RegisterRestClient(configKey = Constants.CONFIG_ROOT_NAME)
public interface OqmCoreApiClientService {
	
	@Path("/q/health")
	@GET
	JsonObject getApiServerHealth();
	
}
