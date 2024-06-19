package tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices;


import tech.ebp.openQuarterMaster.plugin.productSearch.model.ExtItemLookupProviderInfo;

public abstract class ItemSearchService {
	
	public abstract ExtItemLookupProviderInfo getProviderInfo();
	
	public abstract boolean isEnabled();
}
