package tech.ebp.oqm.core.baseStation.service.extItemLookup.searchServices;

import tech.ebp.oqm.core.baseStation.service.extItemLookup.ExtItemLookupProviderInfo;

public abstract class ItemSearchService {
	
	public abstract ExtItemLookupProviderInfo getProviderInfo();
	
	public abstract boolean isEnabled();
}
