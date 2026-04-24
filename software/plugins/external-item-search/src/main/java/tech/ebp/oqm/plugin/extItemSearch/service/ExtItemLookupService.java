package tech.ebp.oqm.plugin.extItemSearch.service;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemSearch;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ResultType;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.barcodeLookup.BarcodeLookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.dataKick.DatakickService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.rebrickable.RebrickableService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.upcItemDb.UpcItemDbService;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;

import java.util.*;

@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class ExtItemLookupService {
	
	private static <T extends Collection<? extends ItemSearchService>> List<ExtItemLookupProviderInfo> servicesToInfoList(T services) {
		List<ExtItemLookupProviderInfo> output = new ArrayList<>(services.size());
		
		for (ItemSearchService curService : services) {
			output.add(curService.getProviderInfo());
		}
		output.sort(ExtItemLookupProviderInfo.Comparator.INSTANCE);
		return output;
	}
	
	Set<ItemSearchService> searchServices = new HashSet<>();
	
	@Inject
	public ExtItemLookupService(
		DatakickService datakickService,
		RebrickableService rebrickableService,
		BarcodeLookupService barcodeLookupService,
		UpcItemDbService upcItemDbService
	) {
		this.searchServices.add(datakickService);
		this.searchServices.add(rebrickableService);
		this.searchServices.add(barcodeLookupService);
		this.searchServices.add(upcItemDbService);
	}
	
	public List<ExtItemLookupProviderInfo> getProductProviderInfo() {
		return servicesToInfoList(this.searchServices);
	}
	
	
	
	public Multi<LookupResult> search(ExtItemSearch search) {
		List<Multi<LookupResult>> resultUnis = new ArrayList<>(this.searchServices.size());
		
		for (ItemSearchService curService : this.searchServices) {
			if(search.getServices().isEmpty() || search.getServices().contains(curService.getProviderInfo().getId()))
				resultUnis.add(
					curService.search(
						search.getSources(),
						search.getLookupMethods(),
						search.getSearch()
					)
				);
		}
		
		Multi<LookupResult> output = Multi.createBy().merging().streams(
			resultUnis.stream()
				.toList()
		);
		
		if(!search.isKeepNotFound()){
			output = output.filter(r -> !r.getType().equals(ResultType.NO_RESULTS));
		}
		
		return output;
	}
}
