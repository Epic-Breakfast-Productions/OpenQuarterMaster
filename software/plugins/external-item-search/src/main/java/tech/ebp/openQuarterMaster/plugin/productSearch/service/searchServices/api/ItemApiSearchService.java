package tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.api;

import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.openQuarterMaster.plugin.productSearch.model.ExtItemLookupResult;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.ItemSearchService;

import java.util.List;

public abstract class ItemApiSearchService extends ItemSearchService {
	public abstract List<ExtItemLookupResult> jsonNodeToSearchResults(JsonNode results);
}
