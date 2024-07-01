package tech.ebp.oqm.plugin.extItemSearch.service.searchServices;


import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;

public abstract class ItemSearchService {
	
	public abstract ExtItemLookupProviderInfo getProviderInfo();
	
	public abstract boolean isEnabled();
}
