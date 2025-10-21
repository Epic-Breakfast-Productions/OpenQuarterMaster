package tech.ebp.oqm.lib.core.api.java.utils;

import lombok.NoArgsConstructor;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import static tech.ebp.oqm.lib.core.api.java.utils.Constants.API_V1_DB_PATH;

/**
 * Utils related to uris
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class PathUtils {
	
	public static String getDbPath(String oqmDbIdOrName){
		return API_V1_DB_PATH.replace("[db]", oqmDbIdOrName);
	}
	
	public static String getInventoryPath(String oqmDbIdOrName){
		return getDbPath(oqmDbIdOrName) + "/inventory";
	}
	
	public static String getStorageBlockPath(String oqmDbIdOrName){
		return getInventoryPath(oqmDbIdOrName) + "/storage-block";
	}
	
	public static String getStorageBlockPath(String oqmDbIdOrName, String storageBlockId){
		return getStorageBlockPath(oqmDbIdOrName) + "/" + storageBlockId;
	}
	
	public static String getInventoryItemPath(String oqmDbIdOrName){
		return getInventoryPath(oqmDbIdOrName) + "/inventory-item";
	}
	
	public static String getInventoryItemPath(String oqmDbIdOrName, String itemId){
		return getInventoryItemPath(oqmDbIdOrName) + "/" + itemId;
	}
	
	public static String getInventoryItemInBlockPath(String oqmDbIdOrName, String itemId, String storageBlockId){
		return getInventoryItemPath(oqmDbIdOrName, itemId) + "/block/" + storageBlockId;
	}
	public static String getInventoryItemInBlockStoredPath(String oqmDbIdOrName, String itemId, String storageBlockId){
		return getInventoryItemInBlockPath(oqmDbIdOrName, itemId, storageBlockId) + "/stored";
	}
	
	public static String getInventoryItemStoredPath(String oqmDbIdOrName, String itemId){
		return getInventoryItemPath(oqmDbIdOrName, itemId) + "/stored";
	}
	
	public static String getInventoryItemStoredPath(String oqmDbIdOrName, String itemId, String storedId){
		return getInventoryItemStoredPath(oqmDbIdOrName, itemId) + "/" + storedId;
	}
	
	public static String getInventoryItemTransactionPath(String oqmDbIdOrName, String itemId){
		return getInventoryItemStoredPath(oqmDbIdOrName, itemId) + "/transaction";
	}
	public static String getInventoryItemTransactionPath(String oqmDbIdOrName, String itemId, String transactionId){
		return getInventoryItemTransactionPath(oqmDbIdOrName, itemId) + "/" + transactionId;
	}
}
