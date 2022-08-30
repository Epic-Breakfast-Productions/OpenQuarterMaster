package com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.lego;

import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.ItemSearchService;
import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class LegoLookupService extends ItemSearchService {
	
	protected abstract CompletionStage<JsonNode> performPartNumberSearchCall(String barcode);
	
	public Optional<CompletableFuture<List<ExtItemLookupResult>>> searchPartNumber(String barcode){
		if(!this.isEnabled()){
			return Optional.empty();
		}
		CompletionStage<JsonNode> stage = this.performPartNumberSearchCall(barcode);
		
		return Optional.of(stage.thenApply(this::jsonNodeToSearchResults).toCompletableFuture());
	}

}
