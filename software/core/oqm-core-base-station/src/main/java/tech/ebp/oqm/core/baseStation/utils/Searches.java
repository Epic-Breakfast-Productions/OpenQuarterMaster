package tech.ebp.oqm.core.baseStation.utils;

import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StoredSearch;

public class Searches {
	public static final StorageBlockSearch BLOCK_PARENT_SEARCH = StorageBlockSearch.builder().isParent(true).build();
	public static final InventoryItemSearch ITEM_LOW_STOCK_SEARCH = InventoryItemSearch.builder().hasLowStock(true).build();
	public static final InventoryItemSearch ITEM_EXPIRY_WARN_SEARCH = InventoryItemSearch.builder().hasExpiryWarn(true).build();
	public static final InventoryItemSearch ITEM_EXPIRED_SEARCH = InventoryItemSearch.builder().hasExpired(true).build();
	
	public static final StoredSearch ITEM_STORED_LOW_STOCK_SEARCH = StoredSearch.builder().hasLowStock(true).build();
	public static final StoredSearch ITEM_STORED_EXPIRY_WARN_SEARCH = StoredSearch.builder().hasExpiryWarn(true).build();
	public static final StoredSearch ITEM_STORED_EXPIRED_SEARCH = StoredSearch.builder().hasExpired(true).build();
}
