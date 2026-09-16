package tech.ebp.oqm.core.baseStation.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.plugins.externalItemSearch.ItemLookupRestInterface;

@Path("/api/v1")
@RegisterRestClient(configKey = "externalItemSearch")
public interface ExternalItemSearchClient {
	
	@GET
	@Path("/info/providers")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ArrayNode> allProviderInfo();
	
	@GET
	@Path("/info/methods")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> allMethodInfo();

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ArrayNode> search(@BeanParam ItemLookupRestInterface.ItemLookupRequest request);

}
