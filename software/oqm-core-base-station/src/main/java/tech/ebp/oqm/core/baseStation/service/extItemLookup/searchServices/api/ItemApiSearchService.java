package tech.ebp.oqm.core.baseStation.service.extItemLookup.searchServices.api;

import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.core.baseStation.service.extItemLookup.ExtItemLookupResult;
import tech.ebp.oqm.core.baseStation.service.extItemLookup.searchServices.ItemSearchService;

import java.util.List;

public abstract class ItemApiSearchService extends ItemSearchService {
	public abstract List<ExtItemLookupResult> jsonNodeToSearchResults(JsonNode results);
}
