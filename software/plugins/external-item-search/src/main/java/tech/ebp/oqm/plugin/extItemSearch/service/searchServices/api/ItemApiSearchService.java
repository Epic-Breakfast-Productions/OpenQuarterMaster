package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.api;

import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;

import java.util.List;

public abstract class ItemApiSearchService extends ItemSearchService {
	public abstract List<ExtItemLookupResult> jsonNodeToSearchResults(JsonNode results);
}
