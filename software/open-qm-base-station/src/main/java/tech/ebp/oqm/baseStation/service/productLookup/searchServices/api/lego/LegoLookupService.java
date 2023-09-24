package tech.ebp.oqm.baseStation.service.productLookup.searchServices.api.lego;

import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.baseStation.model.rest.externalItemLookup.ExtItemLookupResult;
import tech.ebp.oqm.baseStation.service.productLookup.searchServices.api.ItemApiSearchService;

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
