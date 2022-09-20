package tech.ebp.oqm.baseStation.service.productLookup.searchServices;

import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupProviderInfo;

public abstract class ItemSearchService {
	
	public abstract ExtItemLookupProviderInfo getProviderInfo();
	
	public abstract boolean isEnabled();
}
