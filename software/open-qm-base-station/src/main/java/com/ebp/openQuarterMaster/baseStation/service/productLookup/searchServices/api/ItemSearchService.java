package com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api;

import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupProviderInfo;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupResult;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public abstract class ItemSearchService {
	
	public abstract ProductLookupProviderInfo getProviderInfo();
	
	public abstract boolean isEnabled();
	
	public abstract List<ProductLookupResult> jsonNodeToSearchResults(JsonNode results);
}
