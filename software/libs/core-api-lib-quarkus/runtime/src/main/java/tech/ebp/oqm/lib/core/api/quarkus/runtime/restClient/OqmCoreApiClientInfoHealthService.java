package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.INV_ITEM_ROOT_ENDPOINT;
import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.ROOT_API_ENDPOINT_V1;
import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.STORAGE_BLOCK_ROOT_ENDPOINT;


@RegisterRestClient(configKey = Constants.CONFIG_ROOT_NAME)
public interface OqmCoreApiClientInfoHealthService {
	
	@GET
	@Path("/q/health")
	ObjectNode getApiServerHealth();
	
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
	Uni<ArrayNode> searchStorageBlocks(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @BeanParam StorageBlockSearch storageBlockSearch);
	
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/stats")
	Uni<ObjectNode> getStorageBlockStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{blockId}")
	Uni<ObjectNode> getStorageBlockStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("blockId") String storageBlockId);
	//</editor-fold>
	
	//<editor-fold desc="Inventory Items">
	@GET
	@Path(INV_ITEM_ROOT_ENDPOINT + "/stats")
	Uni<ObjectNode> getItemStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//</editor-fold>
}
