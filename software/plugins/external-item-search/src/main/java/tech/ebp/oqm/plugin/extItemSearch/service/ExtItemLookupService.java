package tech.ebp.oqm.plugin.extItemSearch.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemSearch;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.dataKick.DatakickService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.rebrickable.RebrickableService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.ItemKind;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupType;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;

import java.util.*;
import java.util.stream.Stream;

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
		RebrickableService rebrickableService
	) {
		this.searchServices.add(datakickService);
		this.searchServices.add(rebrickableService);
	}
	
	public List<ExtItemLookupProviderInfo> getProductProviderInfo() {
		return servicesToInfoList(this.searchServices);
	}
	
	public List<ItemSearchService> searchServicesMatching(ExtItemSearch search) {
		Stream<ItemSearchService> pending = this.searchServices.stream();
		
		if (search.getServices() != null && !search.getServices().isEmpty()) {
			pending = pending.filter(curService->
										 search.getServices().contains(curService.getProviderInfo().getId())
			);
		}
		if (search.getItemKinds() != null && !search.getItemKinds().isEmpty()) {
			pending = pending.filter(curService->{
					Collection<ItemKind> supportedKinds = curService.getProviderInfo().getKinds();
					
					if (supportedKinds.isEmpty()) {
						return true;
					}
					
					return search.getItemKinds().stream().anyMatch(supportedKinds::contains);
				}
			);
		}
		if (search.getItemBrands() != null && !search.getItemBrands().isEmpty()) {
			pending = pending.filter(curService->{
					Collection<String> supportedBrands = curService.getProviderInfo().getBrands();
					
					if (supportedBrands.isEmpty()) {
						return true;
					}
					
					return search.getItemBrands().stream().anyMatch(supportedBrands::contains);
				}
			);
		}
		
		return pending.toList();
	}
	
	
	public Multi<LookupResult> search(ExtItemSearch search) {
		
		List<Optional<Multi<LookupResult>>> resultUnis = new ArrayList<>(this.searchServices.size());
		
		for (ItemSearchService curService : this.searchServicesMatching(search)) {
			if (search.getLookupTypes() == null || search.getLookupTypes().isEmpty()) {
				resultUnis.add(curService.search(LookupType.FREE_TEXT, search.getSearch()));
				continue;
			}
			
			for (LookupType curType : search.getLookupTypes()) {
				resultUnis.add(
					curService.search(
						curType,
						search.getSearch()
					)
				);
			}
		}
		
		return Multi.createBy().merging().streams(
			resultUnis.stream()
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList()
		);
	}
}
