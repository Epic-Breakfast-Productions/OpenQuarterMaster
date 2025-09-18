package tech.ebp.oqm.lib.core.api.java;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import tech.ebp.oqm.lib.core.api.java.auth.OqmCredentials;
import tech.ebp.oqm.lib.core.api.java.config.CoreApiConfig;
import tech.ebp.oqm.lib.core.api.java.utils.UriUtils;
import tech.ebp.oqm.lib.core.api.java.utils.jackson.JacksonObjectNodeBodyHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static tech.ebp.oqm.lib.core.api.java.utils.Constants.API_V1_PATH;

@Data
@Builder
@Setter(AccessLevel.PRIVATE)
public class OqmCoreApiClient {
	
	@NonNull
	@Builder.Default
	private HttpClient httpClient = HttpClient.newHttpClient();
	
	@NonNull
	private CoreApiConfig config;
	
	@Builder.Default
	private OqmCredentials defaultCredentials = null;
	
	private HttpRequest.Builder setupRequest(
		OqmCredentials creds,
		String path,
		Map<String, String> queryParams
	) {
		HttpRequest.Builder reqBuilder = HttpRequest.newBuilder();
		
		reqBuilder.uri(UriUtils.buildUri(
			this.getConfig().getBaseUri(),
			path,
			queryParams
		));
		
		if (creds != null) {
			reqBuilder.header("Authorization", creds.getAccessHeaderContent());
		}
		
		return reqBuilder;
	}
	
	private HttpRequest.Builder setupRequest(
		OqmCredentials creds,
		String path
	) {
		return this.setupRequest(creds, path, Map.of());
	}
	
	//<editor-fold desc="Info">
	
	public CompletableFuture<HttpResponse<ObjectNode>> serverHealthGet() {
		return this.getHttpClient()
				   .sendAsync(
					   this.setupRequest(null, "/q/health")
						   .GET()
						   .build(),
					   JacksonObjectNodeBodyHandler.INSTANCE
				   );
	}
	
	public CompletableFuture<HttpResponse<String>> infoCurrencyGet() {
		return this.getHttpClient()
				   .sendAsync(
					   this.setupRequest(null, API_V1_PATH + "/info/currency")
						   .GET()
						   .build(),
					   HttpResponse.BodyHandlers.ofString()
				   );
	}
	//</editor-fold>
	
	//<editor-fold desc="General Identifiers">
	//	@GET
	//	@Path(ROOT_API_ENDPOINT_V1 + "/identifiers/general/validate/{type}/{identifier}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> generalIdValidateGet(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("type") String type, @PathParam("identifier") String code);
	//
	//	@GET
	//	@Path(ROOT_API_ENDPOINT_V1 + "/identifiers/general/getIdObject/{identifier}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> generalIdGetObj(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("identifier") String code);
	//
	//	@GET
	//	@Path("/api/media/code/generalId/{type}/{value}")
	//	@Produces("image/svg+xml")
	//	Uni<String> generalIdGetBarcodeImage(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("type") String generalIdType, @PathParam("value") String data);
	//	//</editor-fold>
	
	//	//<editor-fold desc="Interacting Entity">
	
	public CompletableFuture<HttpResponse<ObjectNode>> interactingEntitySearch(OqmCredentials creds) {
		return this.getHttpClient()
				   .sendAsync(
					   this.setupRequest(creds, API_V1_PATH + "/interacting-entity")
						   .GET()
						   .build(),
					   JacksonObjectNodeBodyHandler.INSTANCE
				   );
	}
	
	//	@GET
	//	@Path(ROOT_API_ENDPOINT_V1 + "/interacting-entity/{id}")
	//	Uni<ObjectNode> interactingEntityGet(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("id") String entityId);
	//
	//	@GET
	//	@Path(ROOT_API_ENDPOINT_V1 + "/interacting-entity/{id}/reference")
	//	Uni<ObjectNode> interactingEntityGetReference(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("id") String entityId);
	//
	//	@GET
	//	@Path(ROOT_API_ENDPOINT_V1 + "/interacting-entity/self")
	//	Uni<ObjectNode> interactingEntityGetSelf(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//</editor-fold>
	
	//<editor-fold desc="Units">
	//	@GET
	//	@Path(UNIT_ROOT_ENDPOINT)
	//	@PermitAll
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> unitGetAll(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//
	//	@GET
	//	@Path(UNIT_ROOT_ENDPOINT + "/dimensions")
	//	@PermitAll
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ArrayNode> unitGetDimensions(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//
	//	@GET
	//	@Path(UNIT_ROOT_ENDPOINT + "/deriveTypes")
	//	@PermitAll
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ArrayNode> unitGetDeriveTypes(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//
	//	@GET
	//	@Path(UNIT_ROOT_ENDPOINT + "/compatibility")
	//	@PermitAll
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> unitGetCompatibleMap(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//
	//	@GET
	//	@Path(UNIT_ROOT_ENDPOINT + "/compatibility/{unit}")
	//	@PermitAll
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ArrayNode> unitGetUnitCompatibleWith(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("unit") String unitString
	//	);
	//
	//	@GET
	//	@Path(UNIT_ROOT_ENDPOINT + "/custom")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> unitCustomGetAll(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token
	//	);
	//
	//	@POST
	//	@Path(UNIT_ROOT_ENDPOINT + "/custom")
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<String> unitCreateCustomUnit(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		ObjectNode ncur
	//	);
	//
	//	@PUT
	//	@Path(UNIT_ROOT_ENDPOINT + "/convert")
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<String> unitConvertQuantity(
	//		JsonNode quantityConvertRequest
	//	);
	//</editor-fold>
	
	//<editor-fold desc="Storage Blocks">
	//	@GET
	//	@Path(STORAGE_BLOCK_ROOT_ENDPOINT)
	//	Uni<ObjectNode> storageBlockSearch(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam StorageBlockSearch storageBlockSearch
	//	);
	//
	//	@POST
	//	@Path(STORAGE_BLOCK_ROOT_ENDPOINT)
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<String> storageBlockAdd(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, ObjectNode newStorageBlock);
	//
	//	@POST
	//	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/bulk")
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<String> storageBlockAddBulk(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, ArrayNode newStorageBlocks);
	//
	//	@GET
	//	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/tree")
	//	@Produces({MediaType.APPLICATION_JSON})
	//	Uni<ObjectNode> storageBlockTree(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@QueryParam("onlyInclude") List<String> onlyInclude
	//	);
	//
	//	@GET
	//	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/stats")
	//	Uni<ObjectNode> storageBlockCollectionStats(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName);
	//
	//	@GET
	//	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{blockId}")
	//	Uni<ObjectNode> storageBlockGet(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("blockId") String storageBlockId
	//	);
	//
	//	@PUT
	//	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{id}")
	//	Uni<ObjectNode> storageBlockUpdate(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String storageBlockId,
	//		ObjectNode updates
	//	);
	//
	//	@DELETE
	//	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{id}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> storageBlockDelete(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String storageBlockId
	//	);
	//
	//	@GET
	//	@Path(STORAGE_BLOCK_ROOT_ENDPOINT + "/{blockId}/history")
	//	Uni<ObjectNode> storageBlockGetHistory(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("blockId") String storageBlockId,
	//		@BeanParam HistorySearch historySearch
	//	);
	//</editor-fold>
	
	//<editor-fold desc="Item Categories">
	//	@GET
	//	@Path(ITEM_CAT_ROOT_ENDPOINT)
	//	Uni<ObjectNode> itemCatSearch(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam ItemCategorySearch itemCategorySearch
	//	);
	//
	//	@POST
	//	@Path(ITEM_CAT_ROOT_ENDPOINT)
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<String> itemCatAdd(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, ObjectNode newItemCategory);
	//
	//	@GET
	//	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{catId}")
	//	Uni<ObjectNode> itemCatGet(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @PathParam("catId") String itemCatId);
	//
	//	@PUT
	//	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{catId}")
	//	Uni<ObjectNode> itemCatUpdate(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("catId") String itemCatId,
	//		ObjectNode updates
	//	);
	//
	//	@DELETE
	//	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{id}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> itemCatDelete(@HeaderParam(Constants.AUTH_HEADER_NAME) String token, @PathParam("oqmDbIdOrName") String oqmDbIdOrName, @PathParam("id") String itemCatId);
	//
	//	@GET
	//	@Path(ITEM_CAT_ROOT_ENDPOINT + "/{catId}/history")
	//	Uni<ObjectNode> itemCatGetHistory(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("catId") String catId,
	//		@BeanParam HistorySearch historySearch
	//	);
	//
	//	@GET
	//	@Path(ITEM_CAT_ROOT_ENDPOINT + "/tree")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> itemCatTree(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@QueryParam("onlyInclude") List<String> onlyInclude
	//	);
	
	//</editor-fold>
	
	//<editor-fold desc="Inventory Items">
	//	@POST
	//	@Path(INV_ITEM_ROOT_ENDPOINT)
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<String> invItemCreate(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		ObjectNode item
	//	);
	//
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/stats")
	//	@GET
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemCollectionStats(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemSearch(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		//for actual queries
	//		@BeanParam InventoryItemSearch itemSearch
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemGet(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String id
	//	);
	//
	//	@PUT
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemUpdate(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String id,
	//		ObjectNode updates
	//	);
	//
	//	@DELETE
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemDelete(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String id
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{id}/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemGetHistoryForObject(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String id,
	//		@BeanParam HistorySearch searchObject
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemSearchHistory(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam HistorySearch searchObject
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/block/{storageBlockId}/stored")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredInBlockSearch(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String itemId,
	//		@PathParam("storageBlockId") String storageBlockId,
	//		@BeanParam StoredSearch storedSearch
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredSearch(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String itemId,
	//		@BeanParam StoredSearch storedSearch
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/{storedId}/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredSearchHistory(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String itemId,
	//		@PathParam("storedId") String storedId,
	//		HistorySearch search
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/{storedId}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredGet(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String itemId,
	//		@PathParam("storedId") String storedId
	//	);
	//
	//	@PUT
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/{storedId}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredUpdate(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String itemId,
	//		@PathParam("storedId") String storedId,
	//		ObjectNode updateObject
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredSearchAllHistory(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String itemId,
	//		HistorySearch search
	//	);
	//
	//	@POST
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/transaction")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<String> invItemStoredTransact(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String itemId,
	//		ObjectNode transaction
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/transaction")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredTransactionSearch(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String itemId,
	//		AppliedTransactionSearch search
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_ROOT_ENDPOINT + "/{itemId}/stored/transaction/{transactionId}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredTransactionGet(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("itemId") String itemId,
	//		@PathParam("transactionId") String transactionId
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_STORED_ROOT_ENDPOINT)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredSearch(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam StoredSearch storedSearch
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_STORED_ROOT_ENDPOINT + "/{storedId}/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredSearchHistory(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("storedId") String storedId,
	//		HistorySearch search
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_STORED_ROOT_ENDPOINT + "/{storedId}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredGet(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("storedId") String storedId
	//	);
	//
	//	@PUT
	//	@Path(INV_ITEM_STORED_ROOT_ENDPOINT + "/{storedId}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredUpdate(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("storedId") String storedId,
	//		ObjectNode updateObject
	//	);
	//
	//	@GET
	//	@Path(INV_ITEM_STORED_ROOT_ENDPOINT + "/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> invItemStoredSearchAllHistory(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		HistorySearch search
	//	);
	
	//</editor-fold>
	
	//<editor-fold desc="Images">
	//	@GET
	//	@Path(IMAGE_ROOT_ENDPOINT)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> imageSearch(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam ImageSearch searchObject
	//	);
	//
	//	@POST
	//	@Path(IMAGE_ROOT_ENDPOINT)
	//	@Consumes(MediaType.MULTIPART_FORM_DATA)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<String> imageAdd(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam FileUploadBody body
	//	);
	//
	//	@Path(IMAGE_ROOT_ENDPOINT + "/{id}")
	//	@GET
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> imageGet(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String id
	//	);
	//
	//	@PUT
	//	@Path(IMAGE_ROOT_ENDPOINT + "/{id}")
	//	@Consumes(MediaType.MULTIPART_FORM_DATA)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<Integer> imageUpdateFile(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String id,
	//		@BeanParam FileUploadBody body
	//	);
	//
	//	@PUT
	//	@Path(IMAGE_ROOT_ENDPOINT + "/{id}")
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> imageUpdateObj(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id")
	//		String id,
	//		ObjectNode updates
	//	);
	//
	//	@Path(IMAGE_ROOT_ENDPOINT + "/{id}/revision/{rev}")
	//	@GET
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> imageGetRevision(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id")
	//		String id,
	//		@PathParam("rev")
	//		String revision
	//	);
	//
	//	@Path(IMAGE_ROOT_ENDPOINT + "/{id}/revision/{rev}/data")
	//	@GET
	//	@Produces("*/*")
	//	Uni<Response> imageGetRevisionData(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id")
	//		String id,
	//		@PathParam("rev")
	//		String revision
	//	);
	//
	//	@GET
	//	@Path(IMAGE_ROOT_ENDPOINT + "/{id}/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> imageGetHistoryForObject(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String id,
	//		@BeanParam HistorySearch searchObject
	//	);
	//
	//	@GET
	//	@Path(IMAGE_ROOT_ENDPOINT + "/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> imageSearchHistory(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam HistorySearch searchObject
	//	);
	//
	//	//TODO:: what return datatype?
	//	@GET
	//	@Path(IMAGE_ROOT_ENDPOINT + "/for/{type}/{id}")
	//	@Produces({
	//		"image/png",
	//		"text/plain"
	//	})
	//	Uni<Response> imageForObject(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("type") String type,
	//		@PathParam("id") String objId
	//	);
	//</editor-fold>
	
	//<editor-fold desc="File Attachments">
	//	@GET
	//	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> fileAttachmentSearch(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam FileAttachmentSearch searchObject
	//	);
	//
	//	@POST
	//	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT)
	//	@Consumes(MediaType.MULTIPART_FORM_DATA)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<String> fileAttachmentAdd(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam FileUploadBody body
	//	);
	//
	//	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	//	@GET
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> fileAttachmentGet(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String id
	//	);
	//
	//	@PUT
	//	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	//	@Consumes(MediaType.MULTIPART_FORM_DATA)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<Integer> fileAttachmentUpdateFile(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String id,
	//		@BeanParam FileUploadBody body
	//	);
	//
	//	@PUT
	//	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> fileAttachmentUpdateObj(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id")
	//		String id,
	//		ObjectNode updates
	//	);
	//
	//	@GET
	//	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}/revision/{rev}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> fileAttachmentGetRevision(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id")
	//		String id,
	//		@PathParam("rev")
	//		String revision
	//	);
	//
	//	@GET
	//	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}/revision/{rev}/data")
	//	@Produces("*/*")
	//	Uni<Response> fileAttachmentGetRevisionData(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id")
	//		String id,
	//		@PathParam("rev")
	//		String revision
	//	);
	//
	//	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}")
	//	@DELETE
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> fileAttachmentRemove(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id")
	//		String id
	//	);
	//
	//	@GET
	//	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/{id}/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> fileAttachmentGetHistoryForObject(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String id,
	//		@BeanParam HistorySearch searchObject
	//	);
	//
	//	@GET
	//	@Path(FILE_ATTACHMENT_ROOT_ENDPOINT + "/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<SearchObject> fileAttachmentSearchHistory(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam HistorySearch searchObject
	//	);
	//</editor-fold>
	
	//<editor-fold desc="Item Checkouts">
	//	@GET
	//	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> itemCheckoutSearch(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam ItemCheckoutSearch itemCheckoutSearch
	//	);
	//
	//	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT + "/{id}")
	//	@GET
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> itemCheckoutGet(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String id
	//	);
	//
	//	@PUT
	//	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT + "/{id}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> itemCheckoutUpdate(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String id,
	//		ObjectNode updates
	//	);
	//
	//	@GET
	//	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT + "/{id}/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> itemCheckoutGetHistoryForObject(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@PathParam("id") String id,
	//		@BeanParam HistorySearch searchObject
	//	);
	//
	//	@GET
	//	@Path(ITEM_CHECKOUT_ROOT_ENDPOINT + "/history")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> itemCheckoutSearchHistory(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName") String oqmDbIdOrName,
	//		@BeanParam HistorySearch searchObject
	//	);
	//</editor-fold>
	
	//<editor-fold desc="Inventory Management">
	//	@GET
	//	@Path(INVENTORY_MANAGE_ROOT_ENDPOINT + "/export")
	//	@Produces("application/tar+gzip")
	//	Uni<Response> manageExportData(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@QueryParam("excludeHistory") boolean excludeHistory
	//	);
	//
	//	@POST
	//	@Path(INVENTORY_MANAGE_ROOT_ENDPOINT + "/import/file/bundle")
	//	@Consumes(MediaType.MULTIPART_FORM_DATA)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> manageImportData(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@BeanParam ImportBundleFileBody body
	//	);
	//
	//	@POST
	//	@Path(INVENTORY_MANAGE_ROOT_ENDPOINT + "/db")
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<String> manageDbAdd(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		ObjectNode newDb
	//	);
	//
	//	@GET
	//	@Path(INVENTORY_MANAGE_ROOT_ENDPOINT + "/db")
	//	@Consumes(MediaType.APPLICATION_JSON)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ArrayNode> manageDbList(@HeaderParam(Constants.AUTH_HEADER_NAME) String token);
	//
	//	@DELETE
	//	@Path(INVENTORY_MANAGE_ROOT_ENDPOINT + "/db/clearAllDbs")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ArrayNode> manageDbClearAll(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token
	//	);
	//
	//	@DELETE
	//	@Path(INVENTORY_MANAGE_ROOT_ENDPOINT + "/db/clear/{oqmDbIdOrName}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	Uni<ObjectNode> manageDbClear(
	//		@HeaderParam(Constants.AUTH_HEADER_NAME) String token,
	//		@PathParam("oqmDbIdOrName")
	//		String oqmDbIdOrName
	//	);
	//
	//</editor-fold>
}
