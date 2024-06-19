package tech.ebp.openQuarterMaster.plugin.productSearch.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.ItemSearchService;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.api.lego.LegoLookupService;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.api.lego.rebrickable.RebrickableService;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.api.product.ApiProductSearchService;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.api.product.barcodeLookup.BarcodeLookupService;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.api.product.dataKick.DataKickService;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.api.product.upcItemDb.UpcItemDbService;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.webPage.AdafruitWebProductScrapeService;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.webPage.AmazonWebProductScrapeService;
import tech.ebp.openQuarterMaster.plugin.productSearch.service.searchServices.webPage.WebPageProductScrapeService;
import tech.ebp.openQuarterMaster.plugin.productSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.openQuarterMaster.plugin.productSearch.model.ExtItemLookupResult;
import tech.ebp.openQuarterMaster.plugin.productSearch.model.ExtItemLookupResults;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class ExtItemLookupService {
	
	private static <T extends Collection<? extends ItemSearchService>> List<ExtItemLookupProviderInfo> servicesToInfoList(T services){
		List<ExtItemLookupProviderInfo> output = new ArrayList<>(services.size());

		for (ItemSearchService curService :services) {
			output.add(curService.getProviderInfo());
		}
		output.sort(ExtItemLookupProviderInfo.Comparator.INSTANCE);
		return output;
	}
	
	private final Set<WebPageProductScrapeService> pageProductSearchServices = new HashSet<>();
	private final Set<ApiProductSearchService> productSearchServices = new HashSet<>();
	private final Set<LegoLookupService> legoSearchServices = new HashSet<>();
	
	@Inject
	public ExtItemLookupService(
		AdafruitWebProductScrapeService adafruitWebProductScrapeService,
		AmazonWebProductScrapeService amazonWebProductScrapeService,
		DataKickService dataKickService,
		BarcodeLookupService barcodeLookupService,
		UpcItemDbService upcItemDbService,
		RebrickableService rebrickableService
	) {
		this.pageProductSearchServices.add(adafruitWebProductScrapeService);
		this.pageProductSearchServices.add(amazonWebProductScrapeService);
		
		this.productSearchServices.add(dataKickService);
		this.productSearchServices.add(barcodeLookupService);
		this.productSearchServices.add(upcItemDbService);
		
		this.legoSearchServices.add(rebrickableService);
	}
	
	private ExtItemLookupResults processRequestsList(Map<String, CompletableFuture<List<ExtItemLookupResult>>> requests) {
		List<ExtItemLookupResult> resultList = new ArrayList<>(requests.size());
		Map<String, String> errList = new HashMap<>();
		
		for (Map.Entry<String, CompletableFuture<List<ExtItemLookupResult>>> curRequest : requests.entrySet()) {
			String curService = curRequest.getKey();
			CompletableFuture<List<ExtItemLookupResult>> curFuture = curRequest.getValue();
			List<ExtItemLookupResult> results;
			
			try {
				results = curFuture.join();
			} catch(CompletionException e) {
				log.error("FAILED to call {} service- ", curService, e);
				errList.put(curService, e.getCause().getMessage());
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
	
	private ExtItemLookupResults processRequestsSingle(Map<String, CompletableFuture<ExtItemLookupResult>> requests) {
		List<ExtItemLookupResult> resultList = new ArrayList<>(requests.size());
		Map<String, String> errList = new HashMap<>();
		
		for (Map.Entry<String, CompletableFuture<ExtItemLookupResult>> curRequest : requests.entrySet()) {
			String curService = curRequest.getKey();
			CompletableFuture<ExtItemLookupResult> curFuture = curRequest.getValue();
			ExtItemLookupResult results;
			
			try {
				results = curFuture.join();
			} catch(CompletionException e) {
				log.error("FAILED to call {} service- ", curService, e);
				errList.put(curService, e.getCause().getMessage());
				continue;
			}
			
			resultList.add(results);
			log.info("Got a result from {}", curService);
		}
		
		return ExtItemLookupResults.builder()
								   .results(resultList)
								   .serviceErrs(errList)
								   .build();
	}

	public Map<String, List<ExtItemLookupProviderInfo>> getAllProviderInfo(){
		return Map.of(
			"product", this.getProductProviderInfo(),
			"lego", this.getLegoProviderInfo(),
			"webpage", this.getSupportedPageScanInfo()
		);
	}
	
	@WithSpan
	public ExtItemLookupResults searchBarcode(String barcode) {
		Map<String, CompletableFuture<List<ExtItemLookupResult>>> resultMap = new HashMap<>();
		
		for (ApiProductSearchService curService : this.productSearchServices) {
			Optional<CompletableFuture<List<ExtItemLookupResult>>> result = curService.searchBarcode(barcode);
			
			if (result.isPresent()) {
				resultMap.put(curService.getProviderInfo().getDisplayName(), result.get());
			}
		}
		
		return this.processRequestsList(resultMap);
	}
	
	public ExtItemLookupResults searchProduct(String brand, String product) {
		//TODO
		return null;
	}
	
	public List<ExtItemLookupProviderInfo> getProductProviderInfo() {
		return servicesToInfoList(this.productSearchServices);
	}
	
	@WithSpan
	public ExtItemLookupResults searchLegoPart(String legoPartNum) {
		Map<String, CompletableFuture<List<ExtItemLookupResult>>> resultMap = new HashMap<>();
		
		for (LegoLookupService curService : this.legoSearchServices) {
			Optional<CompletableFuture<List<ExtItemLookupResult>>> result = curService.searchPartNumber(legoPartNum);
			
			if (result.isPresent()) {
				resultMap.put(curService.getProviderInfo().getDisplayName(), result.get());
			}
		}
		
		return this.processRequestsList(resultMap);
	}
	
	public List<ExtItemLookupProviderInfo> getLegoProviderInfo() {
		return servicesToInfoList(this.legoSearchServices);
	}
	
	/**
	 * TODO:: many sites behind a wall checking for robots...
	 * @param page
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@WithSpan
	public ExtItemLookupResults scanPage(URL page) throws ExecutionException, InterruptedException {
		Map<String, CompletableFuture<ExtItemLookupResult>> resultMap = new HashMap<>();
		
		for (WebPageProductScrapeService curService : this.pageProductSearchServices) {
			if(!curService.canParsePage(page)){
				continue;
			}
			Optional<CompletableFuture<ExtItemLookupResult>> result = curService.scrapeWebPage(page);
			
			if (result.isPresent()) {
				resultMap.put(curService.getProviderInfo().getDisplayName(), result.get());
			}
		}
		
		return this.processRequestsSingle(resultMap);
	}
	
	public List<ExtItemLookupProviderInfo> getSupportedPageScanInfo() {
		return servicesToInfoList(this.pageProductSearchServices);
	}
	
}
