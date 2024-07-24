package tech.ebp.oqm.plugin.mssController.devTools.runtime.restClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.Constants;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.restClient.files.ImportBundleFileBody;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.restClient.searchObjects.*;

import java.util.Currency;
import java.util.List;

import static tech.ebp.oqm.plugin.mssController.devTools.runtime.Constants.*;


@RegisterRestClient(configKey = Constants.CORE_API_CLIENT_NAME)
public interface OqmCoreApiClientService {

	//<editor-fold desc="Info">
	@GET
	@Path("/q/health")
	Uni<ObjectNode> getApiServerHealth();

	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/info/currency")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	Uni<Currency> getCurrency(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//</editor-fold>


	//<editor-fold desc="Interacting Entity">
	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/interacting-entity")
	Uni<ObjectNode> interactingEntitySearch(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @BeanParam InteractingEntitySearch entitySearch);

	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/interacting-entity/{id}")
	Uni<ObjectNode> interactingEntityGet(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("id") String entityId);

	@GET
	@Path(ROOT_API_ENDPOINT_V1 + "/interacting-entity/{id}/reference")
	Uni<ObjectNode> interactingEntityGetReference(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("id") String entityId);

	//</editor-fold>

	//<editor-fold desc="Units">
	@GET
	@Path(UNIT_ROOT_ENDPOINT)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> unitGetAll(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);

	@GET
	@Path(UNIT_ROOT_ENDPOINT + "/dimensions")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ArrayNode> unitGetDimensions(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);

	@GET
	@Path(UNIT_ROOT_ENDPOINT + "/deriveTypes")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ArrayNode> unitGetDeriveTypes(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);

	@GET
	@Path(UNIT_ROOT_ENDPOINT + "/compatibility")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> unitGetCompatibleMap(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);

	@GET
	@Path(UNIT_ROOT_ENDPOINT + "/compatibility/{unit}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ArrayNode> unitGetUnitCompatibleWith(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("unit") String unitString
	);

	@GET
	@Path(UNIT_ROOT_ENDPOINT + "/custom")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> unitCustomGetAll(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token
	);

	@POST
	@Path(UNIT_ROOT_ENDPOINT + "/custom")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> unitCreateCustomUnit(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		ObjectNode ncur
	);
	//</editor-fold>

	//<editor-fold desc="Storage Blocks">
	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT)
	Uni<ObjectNode> storageBlockSearch(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @BeanParam StorageBlockSearch storageBlockSearch);

	@POST
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> storageBlockAdd(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, ObjectNode newStorageBlock);

	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/tree")
	@Produces({MediaType.APPLICATION_JSON})
	Uni<ObjectNode> storageBlockTree(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @QueryParam("onlyInclude") List<String> onlyInclude);

	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/stats")
	Uni<ObjectNode> storageBlockCollectionStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName);

	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{blockId}")
	Uni<ObjectNode> storageBlockGet(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @PathParam("blockId") String storageBlockId);

	@PUT
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{id}")
	Uni<ObjectNode> storageBlockUpdate(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @PathParam("id") String storageBlockId, ObjectNode updates);

	@DELETE
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> storageBlockDelete(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @PathParam("id") String storageBlockId);

	@GET
	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{blockId}/history")
	Uni<ObjectNode> storageBlockGetHistory(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("blockId") String storageBlockId,
		@BeanParam HistorySearch historySearch
	);
	//</editor-fold>

	//<editor-fold desc="Item Categories">
	@GET
	@Path(ITEM_CAT_ROOT_ENDPOINT)
	Uni<ObjectNode> itemCatSearch(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @BeanParam ItemCategorySearch itemCategorySearch);

	@POST
	@Path(ITEM_CAT_ROOT_ENDPOINT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> itemCatAdd(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, ObjectNode newItemCategory);

	@GET
	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{catId}")
	Uni<ObjectNode> itemCatGet(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @PathParam("catId") String itemCatId);

	@PUT
	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{catId}")
	Uni<ObjectNode> itemCatUpdate(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @PathParam("catId") String itemCatId, ObjectNode updates);

	@DELETE
	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> itemCatDelete(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @PathParam("id") String itemCatId);

	@GET
	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{catId}/history")
	Uni<ObjectNode> itemCatGetHistory(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @PathParam("catId") String catId, @BeanParam HistorySearch historySearch);

	@GET
	@Path(ITEM_CAT_ROOT_ENDPOINT + "/tree")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> itemCatTree(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @QueryParam("onlyInclude") List<String> onlyInclude);

	//</editor-fold>

	//<editor-fold desc="Inventory Items">
	@POST
	@Path(INV_ITEM_ROOT_ENDPOINT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> invItemCreate(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		ObjectNode item
	);

	@POST
	@Path(INV_ITEM_ROOT_ENDPOINT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ArrayNode> invItemImportData(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@BeanParam ImportBundleFileBody body
	);

	@Path(INV_ITEM_ROOT_ENDPOINT + "/stats")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemCollectionStats(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName
	);

	@GET
	@Path(INV_ITEM_ROOT_ENDPOINT)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemSearch(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		//for actual queries
		@BeanParam InventoryItemSearch itemSearch
	);

	@Path(INV_ITEM_ROOT_ENDPOINT + "/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemGet(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id
	);

	@PUT
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemUpdate(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id,
		ObjectNode updates
	);

	@DELETE
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemDelete(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id
	);

	@GET
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{id}/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemGetHistoryForObject(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject
	);

	@GET
	@Path(INV_ITEM_ROOT_ENDPOINT + "/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemSearchHistory(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@BeanParam HistorySearch searchObject
	);

	@GET
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/{storageBlockId}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemGetStoredInventoryItem(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("itemId") String itemId,
		@PathParam("storageBlockId") String storageBlockId
	);

	@PUT
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/{storageBlockId}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemAddStoredInventoryItem(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("itemId") String itemId,
		@PathParam("storageBlockId") String storageBlockId,
		JsonNode addObject
	);

	@PUT
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/{storageBlockId}/{storedId}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemAddStoredInventoryItemToStored(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("itemId") String itemId,
		@PathParam("storedId") String storedId,
		@PathParam("storageBlockId") String storageBlockId,
		JsonNode addObject
	);

	@DELETE
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/{storageBlockId}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemSubtractStoredInventoryItem(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("itemId") String itemId,
		@PathParam("storageBlockId") String storageBlockId,
		JsonNode subtractObject
	);

	@DELETE
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/{storageBlockId}/{storedId}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemSubtractStoredInventoryItem(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("itemId") String itemId,
		@PathParam("storedId") String storedId,
		@PathParam("storageBlockId") String storageBlockId,
		JsonNode subtractObject
	);

	@PUT
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/{storageBlockIdFrom}/{storageBlockIdTo}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemTransferStoredInventoryItem(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("itemId") String itemId,
		@PathParam("storageBlockIdFrom") String storageBlockIdFrom,
		@PathParam("storageBlockIdTo") String storageBlockIdTo,
		JsonNode transferObject
	);

	@PUT
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/{storageBlockIdFrom}/{storedIdFrom}/{storageBlockIdTo}/{storedIdTo}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemTransferStoredInventoryItem(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("itemId") String itemId,
		@PathParam("storageBlockIdFrom") String storageBlockIdFrom,
		@PathParam("storedIdFrom") String storedIdFrom,
		@PathParam("storageBlockIdTo") String storageBlockIdTo,
		@PathParam("storedIdTo") String storedIdTo,
		JsonNode transferObject
	);

	@PUT
	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/applyAddSubtractTransfer")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> invItemApplyAddSubtractTransfer(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("itemId") String itemId,
		ObjectNode action
	);
	//</editor-fold>

	//<editor-fold desc="Images">
	@GET
	@Path(IMAGE_ROOT_ENDPOINT)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageSearch(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@BeanParam ImageSearch searchObject
	);

	@POST
	@Path(IMAGE_ROOT_ENDPOINT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> imageAdd(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@BeanParam FileUploadBody body
	);

	@Path(IMAGE_ROOT_ENDPOINT + "/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageGet(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id
	);

	@PUT
	@Path(IMAGE_ROOT_ENDPOINT + "/{id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<Integer> imageUpdateFile(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id,
		@BeanParam FileUploadBody body
	);

	@PUT
	@Path(IMAGE_ROOT_ENDPOINT + "/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageUpdateObj(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id")
		String id,
		ObjectNode updates
	);

	@Path(IMAGE_ROOT_ENDPOINT + "/{id}/revision/{rev}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageGetRevision(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
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
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
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
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject
	);

	@GET
	@Path(IMAGE_ROOT_ENDPOINT + "/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> imageSearchHistory(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @BeanParam HistorySearch searchObject);

	//TODO:: what return datatype?
	@GET
	@Path(IMAGE_ROOT_ENDPOINT + "/for/{type}/{id}")
	@Produces({
		"image/png",
		"text/plain"
	})
	Uni<Response> imageForObject(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @PathParam("type") String type, @PathParam("id") String objId);
	//</editor-fold>

	//<editor-fold desc="File Attachments">
	@GET
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentSearch(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@BeanParam FileAttachmentSearch searchObject
	);

	@POST
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> fileAttachmentAdd(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@BeanParam FileUploadBody body
	);

	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentGet(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id
	);

	@PUT
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<Integer> fileAttachmentUpdateFile(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id,
		@BeanParam FileUploadBody body
	);

	@PUT
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentUpdateObj(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id")
		String id,
		ObjectNode updates
	);

	@GET
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}/revision/{rev}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentGetRevision(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
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
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
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
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id")
		String id
	);

	@GET
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> fileAttachmentGetHistoryForObject(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject
	);

	@GET
	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<SearchObject> fileAttachmentSearchHistory(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@BeanParam HistorySearch searchObject
	);
	//</editor-fold>

	//<editor-fold desc="Item Checkouts">
	@POST
	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> itemCheckoutCreate(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		ObjectNode itemCheckoutRequest
	);

	@PUT
	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT + "/{id}/checkin")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> itemCheckoutCheckin(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id,
		ObjectNode checkInDetails
	);

	@GET
	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> itemCheckoutSearch(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@BeanParam ItemCheckoutSearch itemCheckoutSearch
	);

	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT + "/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> itemCheckoutGet(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id
	);

	@PUT
	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> itemCheckoutUpdate(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id,
		ObjectNode updates
	);

	@DELETE
	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> itemCheckoutDelete(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id
	);

	@GET
	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT + "/{id}/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> itemCheckoutGetHistoryForObject(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject
	);

	@GET
	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT + "/history")
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> itemCheckoutSearchHistory(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
		@BeanParam HistorySearch searchObject
	);
	//</editor-fold>

	//<editor-fold desc="Inventory Management">
	@GET
	@Path(INVENTORY_MANAGE_ROOT_ENDPOINT + "/export")
	@Produces("application/tar+gzip")
	Uni<Response> manageExportData(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@QueryParam("excludeHistory") boolean excludeHistory
	);

	@POST
	@Path(INVENTORY_MANAGE_ROOT_ENDPOINT + "/import/file/bundle")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ObjectNode> manageImportData(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		@BeanParam ImportBundleFileBody body
	);

	@POST
	@Path(INVENTORY_MANAGE_ROOT_ENDPOINT + "/db")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<String> manageDbAdd(
		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
		ObjectNode newDb
	);

	@GET
	@Path(INVENTORY_MANAGE_ROOT_ENDPOINT + "/db")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Uni<ArrayNode> manageDbList(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);


//	@GET
//	@Path("processExpiry")
//	public Response triggerSearchAndProcessExpiring() {
//		expiryProcessor.searchAndProcessExpiring();
//
//		return Response.ok().build();
//	}
	//</editor-fold>
}
