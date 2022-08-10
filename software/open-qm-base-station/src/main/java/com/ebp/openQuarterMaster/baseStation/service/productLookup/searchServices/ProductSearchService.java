package com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices;


import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupProviderInfo;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.opentracing.Traced;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Traced
public abstract class ProductSearchService {
	
	public abstract ProductLookupProviderInfo getProviderInfo();
	
	public abstract boolean isEnabled();
	
	public abstract List<ProductLookupResult> jsonNodeToSearchResults(JsonNode results);
	
	protected abstract CompletionStage<JsonNode> performBarcodeSearchCall(String barcode);
	
	public  Optional<CompletableFuture<List<ProductLookupResult>>> searchBarcode(String barcode){
		if(!this.isEnabled()){
			return Optional.empty();
		}
		CompletionStage<JsonNode> stage = this.performBarcodeSearchCall(barcode);
		
		return Optional.of(stage.thenApply(this::jsonNodeToSearchResults).toCompletableFuture());
	}
}
