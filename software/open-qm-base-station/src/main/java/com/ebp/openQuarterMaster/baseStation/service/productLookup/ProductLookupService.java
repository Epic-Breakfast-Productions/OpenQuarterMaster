package com.ebp.openQuarterMaster.baseStation.service.productLookup;

import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.lego.LegoLookupService;
import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.lego.RebrickableService;
import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.product.ApiProductSearchService;
import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.product.BarcodeLookupService;
import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.product.DataKickService;
import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.product.UpcItemDbService;
import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.page.PageProductSearchService;
import com.ebp.openQuarterMaster.lib.core.rest.externalItemLookup.ExtItemLookupProviderInfo;
import com.ebp.openQuarterMaster.lib.core.rest.externalItemLookup.ExtItemLookupResult;
import com.ebp.openQuarterMaster.lib.core.rest.externalItemLookup.ExtItemLookupResults;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
@Slf4j
@Traced
@NoArgsConstructor
public class ProductLookupService {
	
	private PageProductSearchService pageProductSearchService;
	private final Set<ApiProductSearchService> productSearchServices = new HashSet<>();
	private final Set<LegoLookupService> legoSearchServices = new HashSet<>();
	
	@Inject
	public ProductLookupService(
		PageProductSearchService pageProductSearchService,
		DataKickService dataKickService,
		BarcodeLookupService barcodeLookupService,
		UpcItemDbService upcItemDbService,
		RebrickableService rebrickableService
	) {
		this.pageProductSearchService = pageProductSearchService;
		
		this.productSearchServices.add(dataKickService);
		this.productSearchServices.add(barcodeLookupService);
		this.productSearchServices.add(upcItemDbService);
		
		this.legoSearchServices.add(rebrickableService);
	}
	
	
	private ExtItemLookupResults processRequests(Map<String, CompletableFuture<List<ExtItemLookupResult>>> requests) {
		List<ExtItemLookupResult> resultList = new ArrayList<>(requests.size());
		Map<String, Throwable> errList = new HashMap<>();
		
		for (Map.Entry<String, CompletableFuture<List<ExtItemLookupResult>>> curRequest : requests.entrySet()) {
			String curService = curRequest.getKey();
			CompletableFuture<List<ExtItemLookupResult>> curFuture = curRequest.getValue();
			List<ExtItemLookupResult> results;
			
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
		
		
		return ExtItemLookupResults.builder()
								   .results(resultList)
								   .serviceErrs(errList)
								   .build();
	}
	
	public ExtItemLookupResults searchBarcode(String barcode) {
		Map<String, CompletableFuture<List<ExtItemLookupResult>>> resultMap = new HashMap<>();
		
		for (ApiProductSearchService curService : this.productSearchServices) {
			Optional<CompletableFuture<List<ExtItemLookupResult>>> result = curService.searchBarcode(barcode);
			
			if (result.isPresent()) {
				resultMap.put(curService.getProviderInfo().getDisplayName(), result.get());
			}
		}
		
		return this.processRequests(resultMap);
	}
	
	public ExtItemLookupResults searchProduct(String brand, String product) {
		//TODO
		return null;
	}
	
	public ExtItemLookupResults searchLegoPart(String legoPartNum) {
		Map<String, CompletableFuture<List<ExtItemLookupResult>>> resultMap = new HashMap<>();
		
		for (LegoLookupService curService : this.legoSearchServices) {
			Optional<CompletableFuture<List<ExtItemLookupResult>>> result = curService.searchPartNumber(legoPartNum);
			
			if (result.isPresent()) {
				resultMap.put(curService.getProviderInfo().getDisplayName(), result.get());
			}
		}
		
		return this.processRequests(resultMap);
	}
	
	/**
	 * TODO:: many sites behind a wall checking for robots...
	 * @param page
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public ExtItemLookupResults scanPage(URL page) throws ExecutionException, InterruptedException {
		return ExtItemLookupResults.builder().results(List.of(this.pageProductSearchService.scanWebpage(page).get())).build();
	}
	
	public List<ExtItemLookupProviderInfo> getProductProviderInfo() {
		List<ExtItemLookupProviderInfo> output = new ArrayList<>(this.productSearchServices.size());
		
		for (ApiProductSearchService curService : this.productSearchServices) {
			output.add(curService.getProviderInfo());
		}
		
		return output;
	}
	
}
