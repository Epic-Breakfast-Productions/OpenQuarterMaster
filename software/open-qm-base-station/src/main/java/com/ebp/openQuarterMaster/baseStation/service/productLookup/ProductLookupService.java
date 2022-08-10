package com.ebp.openQuarterMaster.baseStation.service.productLookup;

import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.BarcodeLookupService;
import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.DataKickService;
import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.ProductSearchService;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupProviderInfo;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupResult;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupResults;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@ApplicationScoped
@Slf4j
@Traced
@NoArgsConstructor
public class ProductLookupService {
	
	
	private final Set<ProductSearchService> productSearchServices = new HashSet<>();
	
	@Inject
	public ProductLookupService(
		DataKickService dataKickService,
		BarcodeLookupService barcodeLookupService
	) {
		this.productSearchServices.add(dataKickService);
		this.productSearchServices.add(barcodeLookupService);
	}
	
	
	private ProductLookupResults processRequests(Map<String, CompletableFuture<List<ProductLookupResult>>> requests) {
		List<ProductLookupResult> resultList = new ArrayList<>(requests.size());
		Map<String, Throwable> errList = new HashMap<>();
		
		for (Map.Entry<String, CompletableFuture<List<ProductLookupResult>>> curRequest : requests.entrySet()) {
			String curService = curRequest.getKey();
			CompletableFuture<List<ProductLookupResult>> curFuture = curRequest.getValue();
			List<ProductLookupResult> results;
			
			try {
				results = curFuture.join();
			} catch(CompletionException e) {
				log.error("FAILED to call {} service- ", curService, e);
				errList.put(curService, e);
				continue;
			}
			
			resultList.addAll(results);
			log.info("Got {} results from {}", results.size(), curService);
		}
		
		
		return ProductLookupResults.builder()
								   .results(resultList)
								   .serviceErrs(errList)
								   .build();
	}
	
	public ProductLookupResults searchBarcode(String barcode) {
		Map<String, CompletableFuture<List<ProductLookupResult>>> resultMap = new HashMap<>();
		
		for (ProductSearchService curService : this.productSearchServices) {
			Optional<CompletableFuture<List<ProductLookupResult>>> result = curService.searchBarcode(barcode);
			
			if (result.isPresent()) {
				resultMap.put(curService.getProviderInfo().getDisplayName(), result.get());
			}
		}
		
		return this.processRequests(resultMap);
	}
	
	public ProductLookupResults searchProduct(String brand, String product) {
		return null;
	}
	
	public List<ProductLookupProviderInfo> getProviderInfo() {
		List<ProductLookupProviderInfo> output = new ArrayList<>(this.productSearchServices.size());
		
		for (ProductSearchService curService : this.productSearchServices) {
			output.add(curService.getProviderInfo());
		}
		
		return output;
	}
	
}
