package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.Constants;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.AllInfo;

import java.io.InputStream;


@RegisterRestClient(configKey = Constants.REST_CLIENT_NAME)
public interface OqmCoreCharacteristicsRestClient {
	
	@GET
	@Path("/health")
	Uni<ObjectNode> health();
	
	@GET
	@Path("/all")
	Uni<AllInfo> allInfo();
	
	@Path("/characteristics/logo")
	@GET
	@Produces("*/*")
	Uni<InputStream> characteristicsLogo();
	
}
