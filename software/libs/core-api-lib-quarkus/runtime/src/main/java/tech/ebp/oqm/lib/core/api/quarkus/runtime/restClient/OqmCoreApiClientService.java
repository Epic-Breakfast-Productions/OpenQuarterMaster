package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.HistorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

import java.util.List;

import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.IMAGE_ROOT_ENDPOINT;
import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.INV_ITEM_ROOT_ENDPOINT;
import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.ROOT_API_ENDPOINT_V1;
import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.STORAGE_BLOCK_ROOT_ENDPOINT;


@RegisterRestClient(configKey = Constants.CONFIG_ROOT_NAME)
public interface OqmCoreApiClientService {
	
	//<editor-fold desc="Info">
	@GET
	@Path("/q/health")
	Uni<ObjectNode> getApiServerHealth();
	
	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/info/units")
	Uni<ObjectNode> getAllUnits(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	
	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/info/unitCompatibility")
	Uni<ObjectNode> getUnitCompatability(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	
	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/interacting-entity/{id}/reference")
	Uni<ObjectNode> interactingEntityGetReference(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("id") String entityId);
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
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/tree")
	@Produces({MediaType.APPLICATION_JSON})
	Uni<ObjectNode> storageBlockTree(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @QueryParam("onlyInclude") List<String> onlyInclude);
	
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/stats")
	Uni<ObjectNode> storageBlockCollectionStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{blockId}")
	Uni<ObjectNode> storageBlockGet(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("blockId") String storageBlockId);
	
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{blockId}/history")
	Uni<ObjectNode> storageBlockGetHistory(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("blockId") String storageBlockId, @BeanParam HistorySearch historySearch);
	//</editor-fold>
	
	//<editor-fold desc="Inventory Items">
	@GET
	@Path(INV_ITEM_ROOT_ENDPOINT)
	Uni<ObjectNode> invItemSearch(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @BeanParam InventoryItemSearch inventoryItemSearch);
	
	@GET
	@Path(INV_ITEM_ROOT_ENDPOINT + "/stats")
	Uni<ObjectNode> invItemCollectionStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//</editor-fold>
	
	//<editor-fold desc="Images">
	@GET
	@Path(IMAGE_ROOT_ENDPOINT + "/for/{type}/{id}")
	Uni<ObjectNode> imageForObject(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("type") String type,  @PathParam("id") String objId);
	//</editor-fold>
}
