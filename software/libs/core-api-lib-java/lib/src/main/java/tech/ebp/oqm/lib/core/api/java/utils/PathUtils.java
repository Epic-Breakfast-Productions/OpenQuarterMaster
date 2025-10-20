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
}
