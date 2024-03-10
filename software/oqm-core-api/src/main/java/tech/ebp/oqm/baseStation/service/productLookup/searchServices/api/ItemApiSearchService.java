package tech.ebp.oqm.baseStation.service.productLookup.searchServices.api;

import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.baseStation.model.rest.externalItemLookup.ExtItemLookupResult;
import tech.ebp.oqm.baseStation.service.productLookup.searchServices.ItemSearchService;

import java.util.List;

public abstract class ItemApiSearchService extends ItemSearchService {
	public abstract List<ExtItemLookupResult> jsonNodeToSearchResults(JsonNode results);
}