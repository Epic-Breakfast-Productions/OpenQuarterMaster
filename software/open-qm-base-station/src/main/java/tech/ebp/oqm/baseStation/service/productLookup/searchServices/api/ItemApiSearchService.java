package tech.ebp.oqm.baseStation.service.productLookup.searchServices.api;

import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.baseStation.service.productLookup.searchServices.ItemSearchService;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupResult;

import java.util.List;

public abstract class ItemApiSearchService extends ItemSearchService {
	public abstract List<ExtItemLookupResult> jsonNodeToSearchResults(JsonNode results);
}
