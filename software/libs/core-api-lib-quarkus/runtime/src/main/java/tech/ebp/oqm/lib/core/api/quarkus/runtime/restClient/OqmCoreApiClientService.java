package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.FileAttachmentSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.HistorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ImageSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ItemCategorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.SearchObject;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.*;


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
	
	@PUT
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{id}")
	Uni<ObjectNode> storageBlockUpdate(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("id") String storageBlockId, ObjectNode updates);
	
	@DELETE
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> storageBlockDelete(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("id") String storageBlockId);
	
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{blockId}/history")
	Uni<ObjectNode> storageBlockGetHistory(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("blockId") String storageBlockId,
		@BeanParam HistorySearch historySearch
	);
	//</editor-fold>
	
	//<editor-fold desc="Item Categories">
	@GET
	@Path(ITEM_CAT_ROOT_ENDPOINT)
	Uni<ObjectNode> itemCatSearch(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @BeanParam ItemCategorySearch itemCategorySearch);
	
	@POST
	@Path(ITEM_CAT_ROOT_ENDPOINT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> itemCatAdd(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, ObjectNode newItemCategory);
	
	@GET
	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{catId}")
	Uni<ObjectNode> itemCatGet(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("catId") String itemCatId);
	
	@PUT
	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{catId}")
	Uni<ObjectNode> itemCatUpdate(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("catId") String itemCatId, ObjectNode updates);
	
	@DELETE
	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> itemCatDelete(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("id") String itemCatId);
	
	@GET
	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{catId}/history")
	Uni<ObjectNode> itemCatGetHistory(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("catId") String catId, @BeanParam HistorySearch historySearch);
	
	@GET
	@Path(ITEM_CAT_ROOT_ENDPOINT + "/tree")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<ObjectNode> itemCatTree(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @QueryParam("onlyInclude") List<String> onlyInclude);
	
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
	@Path(IMAGE_ROOT_ENDPOINT)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageSearch(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@BeanParam ImageSearch searchObject
	);
	
	@POST
	@Path(IMAGE_ROOT_ENDPOINT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> imageAdd(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@BeanParam FileUploadBody body
	);
	
	@Path(IMAGE_ROOT_ENDPOINT + "/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageGet(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id") String id
	);
	
	@PUT
	@Path(IMAGE_ROOT_ENDPOINT + "/{id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<Integer> imageUpdateFile(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id") String id,
		@BeanParam FileUploadBody body
	);
	
	@PUT
	@Path(IMAGE_ROOT_ENDPOINT + "/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageUpdateObj(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id")
		String id,
		ObjectNode updates
	);
	
	@Path(IMAGE_ROOT_ENDPOINT + "/{id}/revision/{rev}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageGetRevision(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id")
		String id,
		@PathParam("rev")
		String revision
	);
	
	@Path(IMAGE_ROOT_ENDPOINT + "/{id}/revision/{rev}/data")
	@GET
	@Produces("*/*")
	Uni<Response> imageGetRevisionData(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id")
		String id,
		@PathParam("rev")
		String revision
	);
	
	@GET
	@Path(IMAGE_ROOT_ENDPOINT + "/{id}/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageGetHistoryForObject(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject
	);
	
	@GET
	@Path(IMAGE_ROOT_ENDPOINT + "/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageSearchHistory(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @BeanParam HistorySearch searchObject);
	
	//TODO:: what return datatype?
	@GET
	@Path(IMAGE_ROOT_ENDPOINT + "/for/{type}/{id}")
	@Produces({
		"image/png",
		"text/plain"
	})
	Uni<Response> imageForObject(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("type") String type, @PathParam("id") String objId);
	//</editor-fold>
	
	//<editor-fold desc="File Attachments">
	@GET
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentSearch(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@BeanParam FileAttachmentSearch searchObject
	);
	
	@POST
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> fileAttachmentAdd(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@BeanParam FileUploadBody body
	);
	
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentGet(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id") String id
	);
	
	@PUT
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<Integer> fileAttachmentUpdateFile(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id") String id,
		@BeanParam FileUploadBody body
	);
	
	@PUT
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentUpdateObj(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id")
		String id,
		ObjectNode updates
	);
	
	@GET
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}/revision/{rev}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentGetRevision(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id")
		String id,
		@PathParam("rev")
		String revision
	);
	
	@GET
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}/revision/{rev}/data")
	@Produces("*/*")
	Uni<Response> fileAttachmentGetRevisionData(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id")
		String id,
		@PathParam("rev")
		String revision
	);
	
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentRemove(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id")
		String id
	);
	
	@GET
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentGetHistoryForObject(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject
	);
	
	@GET
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<SearchObject> fileAttachmentSearchHistory(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@BeanParam HistorySearch searchObject
	);
	//</editor-fold>
}
