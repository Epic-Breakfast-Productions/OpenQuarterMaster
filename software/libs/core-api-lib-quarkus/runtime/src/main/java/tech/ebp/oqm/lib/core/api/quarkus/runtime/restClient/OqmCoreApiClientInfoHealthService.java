package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

import java.net.http.HttpHeaders;

import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.INV_ITEM_ROOT_ENDPOINT;


@RegisterRestClient(configKey = Constants.CONFIG_ROOT_NAME)
public interface OqmCoreApiClientInfoHealthService {
	
	@Path("/q/health")
	@GET
	ObjectNode getApiServerHealth();
	
	@Path(INV_ITEM_ROOT_ENDPOINT + "/stats")
	@GET
	ObjectNode getItemStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
}
