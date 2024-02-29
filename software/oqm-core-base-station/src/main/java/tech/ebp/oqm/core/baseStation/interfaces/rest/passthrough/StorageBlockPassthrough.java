package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.util.List;

import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.STORAGE_BLOCK_ROOT_ENDPOINT;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/inventory/storage-block")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class StorageBlockPassthrough extends PassthroughProvider {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@POST
	public Uni<Response> addStorageBlock(ObjectNode newStorageBlock){
		return this.oqmCoreApiClient.storageBlockAdd(this.getBearerHeaderStr(), newStorageBlock)
				   .map( output ->
							 Response.ok(output).build()
				   );
	}
	
	@GET
	@Path("{blockId}")
	public Uni<Response> getStorageBlock(@PathParam("blockId") String storageBlockId){
		return this.oqmCoreApiClient.storageBlockGet(this.getBearerHeaderStr(), storageBlockId)
				   .map( output ->
							 Response.ok(output).build()
				   );
	}
	
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/tree")
	@Produces({MediaType.APPLICATION_JSON})
	public Uni<Response> storageBlockTree(@QueryParam("onlyInclude") List<String> onlyInclude){
		return this.oqmCoreApiClient.storageBlockTree(this.getBearerHeaderStr(), onlyInclude)
				   .map( output ->
							 Response.ok(output).build()
				   );
	}
	
}
