package tech.ebp.oqm.baseStation.service.productLookup.searchServices.api.product;


import com.fasterxml.jackson.databind.JsonNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.baseStation.model.rest.externalItemLookup.ExtItemLookupResult;
import tech.ebp.oqm.baseStation.service.productLookup.searchServices.api.ItemApiSearchService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
public abstract class ApiProductSearchService extends ItemApiSearchService {
	
	@WithSpan
	protected abstract CompletionStage<JsonNode> performBarcodeSearchCall(String barcode);
	
	@WithSpan
	public Optional<CompletableFuture<List<ExtItemLookupResult>>> searchBarcode(String barcode) {
		if (!this.isEnabled()) {
			return Optional.empty();
		}
		CompletionStage<JsonNode> stage = this.performBarcodeSearchCall(barcode);
		
		return Optional.of(stage.thenApply(this::jsonNodeToSearchResults).toCompletableFuture());
	}
}
