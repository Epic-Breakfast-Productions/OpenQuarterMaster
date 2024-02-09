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
import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.ROOT_API_ENDPOINT_V1;


@RegisterRestClient(configKey = Constants.CONFIG_ROOT_NAME)
public interface OqmCoreApiClientInfoHealthService {
	
	@GET
	@Path("/q/health")
	ObjectNode getApiServerHealth();
	
	//<editor-fold desc="Info">
	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/info/units")
	ObjectNode getAllUnits(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	
	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/info/unitCompatibility")
	ObjectNode getUnitCompatability(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//</editor-fold>
	
	//<editor-fold desc="Inventory Items">
	@GET
	@Path(INV_ITEM_ROOT_ENDPOINT + "/stats")
	ObjectNode getItemStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//</editor-fold>
}
