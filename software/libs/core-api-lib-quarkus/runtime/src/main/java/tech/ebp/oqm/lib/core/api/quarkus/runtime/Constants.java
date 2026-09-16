package tech.ebp.oqm.lib.core.api.quarkus.runtime;

/**
 * Constants for use around the client.
 */
public class Constants {
	public static final String CONFIG_ROOT_NAME = "oqm.core.api";

	public static final String CORE_API_CLIENT_NAME = CONFIG_ROOT_NAME;
	public static final String CORE_API_CLIENT_OIDC_NAME = CORE_API_CLIENT_NAME + "-oidc";

	
	public static final String AUTH_HEADER_NAME = "Authorization";
	
	private static final String ROOT_API_ENDPOINT = "/api";
	public static final String ROOT_API_ENDPOINT_V1 = ROOT_API_ENDPOINT + "/v1";

	public static final String INV_DB_ROOT_ENDPOINT = ROOT_API_ENDPOINT_V1 + "/db/{oqmDbIdOrName}";

	public static final String UNIT_ROOT_ENDPOINT = ROOT_API_ENDPOINT_V1 + "/unit";
	public static final String STORAGE_BLOCK_ROOT_ENDPOINT = INV_DB_ROOT_ENDPOINT + "/inventory/storage-block";
	public static final String INV_ITEM_ROOT_ENDPOINT = INV_DB_ROOT_ENDPOINT + "/inventory/item";
	public static final String INV_ITEM_STORED_ROOT_ENDPOINT = INV_DB_ROOT_ENDPOINT + "/inventory/item/stored";
	public static final String ITEM_CAT_ROOT_ENDPOINT = INV_DB_ROOT_ENDPOINT + "/inventory/item-category";
	public static final String ITEM_CHECKOUT_ROOT_ENDPOINT = INV_DB_ROOT_ENDPOINT + "/inventory/item-checkout";
	public static final String MEDIA_ROOT_ENDPOINT = INV_DB_ROOT_ENDPOINT + "/media";
	public static final String IMAGE_ROOT_ENDPOINT = MEDIA_ROOT_ENDPOINT + "/image";
	public static final String FILE_ATTACHMENT_ROOT_ENDPOINT = MEDIA_ROOT_ENDPOINT + "/fileAttachment";

	public static final String INVENTORY_MANAGE_ROOT_ENDPOINT = ROOT_API_ENDPOINT_V1 + "/inventory/manage";
	
	
}
