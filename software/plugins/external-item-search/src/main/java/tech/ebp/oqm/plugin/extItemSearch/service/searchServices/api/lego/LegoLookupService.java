package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.api.lego;

import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.api.ItemApiSearchService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class LegoLookupService extends ItemApiSearchService {
	
	protected abstract CompletionStage<JsonNode> performPartNumberSearchCall(String barcode);
	
	public Optional<CompletableFuture<List<ExtItemLookupResult>>> searchPartNumber(String barcode){
		if(!this.isEnabled()){
			return Optional.empty();
		}
		CompletionStage<JsonNode> stage = this.performPartNumberSearchCall(barcode);
		
		return Optional.of(stage.thenApply(this::jsonNodeToSearchResults).toCompletableFuture());
	}

}
