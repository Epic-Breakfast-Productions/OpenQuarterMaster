package tech.ebp.oqm.lib.core.api.quarkus.runtime;

public class Constants {
	public static final String CONFIG_ROOT_NAME = "oqmCoreApi";
	
	public static final String AUTH_HEADER_NAME = "Authorization";
	
	private static final String ROOT_API_ENDPOINT = "/api";
	public static final String ROOT_API_ENDPOINT_V1 = ROOT_API_ENDPOINT + "/v1";
	
	public static final String STORAGE_BLOCK_ROOT_ENDPOINT = ROOT_API_ENDPOINT_V1 + "/inventory/storage-block";
	public static final String INV_ITEM_ROOT_ENDPOINT = ROOT_API_ENDPOINT_V1 + "/inventory/item";
	public static final String ITEM_CAT_ROOT_ENDPOINT = ROOT_API_ENDPOINT_V1 + "/inventory/item-category";
	public static final String UNIT_ROOT_ENDPOINT = ROOT_API_ENDPOINT_V1 + "/inventory/unit";
	public static final String MEDIA_ROOT_ENDPOINT = ROOT_API_ENDPOINT_V1 + "/media";
	public static final String IMAGE_ROOT_ENDPOINT = MEDIA_ROOT_ENDPOINT + "/image";
	public static final String FILE_ATTACHMENT_ROOT_ENDPOINT = MEDIA_ROOT_ENDPOINT + "/fileAttachment";
	public static final String ITEM_CHECKOUT_ROOT_ENDPOINT = ROOT_API_ENDPOINT_V1 + "/inventory/item-checkout";
	
	
}
