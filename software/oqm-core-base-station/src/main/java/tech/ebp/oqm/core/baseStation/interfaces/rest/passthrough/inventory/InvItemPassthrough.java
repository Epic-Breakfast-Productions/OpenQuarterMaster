package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.inventory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.PassthroughProvider;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

import java.util.List;

import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.STORAGE_BLOCK_ROOT_ENDPOINT;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/inventory/item")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class InvItemPassthrough extends PassthroughProvider {
	
	@GET
	public Uni<Response> getStorageBlock(@BeanParam InventoryItemSearch search){
		return this.getOqmCoreApiClient().invItemSearch(this.getBearerHeaderStr(), search)
				   .map( output ->
							 Response.ok(output).build()
				   );
	}
}
