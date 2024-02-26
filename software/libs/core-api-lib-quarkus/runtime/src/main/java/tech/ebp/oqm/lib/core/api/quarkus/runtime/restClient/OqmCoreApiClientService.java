package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.INV_ITEM_ROOT_ENDPOINT;
import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.ROOT_API_ENDPOINT_V1;
import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.STORAGE_BLOCK_ROOT_ENDPOINT;


@RegisterRestClient(configKey = Constants.CONFIG_ROOT_NAME)
public interface OqmCoreApiClientService {
	
	@GET
	@Path("/q/health")
	Uni<ObjectNode> getApiServerHealth();
	
	//<editor-fold desc="Info">
	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/info/units")
	Uni<ObjectNode> getAllUnits(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	
	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/info/unitCompatibility")
	Uni<ObjectNode> getUnitCompatability(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//</editor-fold>
	
	//<editor-fold desc="Storage Blocks">
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT)
	Uni<ObjectNode> storageBlockSearch(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @BeanParam StorageBlockSearch storageBlockSearch);
	
	@POST
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> storageBlockAdd(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, ObjectNode newStorageBlock);
	
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/stats")
	Uni<ObjectNode> storageBlockCollectionStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{blockId}")
	Uni<ObjectNode> storageBlockGet(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("blockId") String storageBlockId);
	//</editor-fold>
	
	//<editor-fold desc="Inventory Items">
	@GET
	@Path(INV_ITEM_ROOT_ENDPOINT + "/stats")
	Uni<ObjectNode> invItemCollectionStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//</editor-fold>
}
