package com.ebp.openQuarterMaster.plugin.moduleInteraction.service;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.ItemSearchResults;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.ModuleMaster;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.MssModule;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.response.ModuleInfo;
import com.ebp.openQuarterMaster.plugin.restClients.KcClientAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;

import java.util.Iterator;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class ItemSearchService {

	@Inject
	ModuleMaster moduleMaster;
	
	@RestClient
	OqmCoreApiClientService coreApiClientService;
	@Inject
	@Getter(AccessLevel.PRIVATE)
	KcClientAuthService kcClientAuthService;
	
	/**
	 * TODO:: logic of what storage blocks are in an item search should be in BaseStation
	 * @param itemSearch
	 * @param doHighlight
	 * @return
	 */
	public ItemSearchResults searchForItemLocations(InventoryItemSearch itemSearch, boolean doHighlight){
		ObjectNode itemResultsJson = this.coreApiClientService.invItemSearch(this.getKcClientAuthService().getAuthString(), itemSearch).await().indefinitely();
		ItemSearchResults output = new ItemSearchResults();
		
		for(JsonNode curItemJson : itemResultsJson.get("results")){
			Iterator<String> curStorageBlockIds = curItemJson.get("storageMap").fieldNames();
			
			while (curStorageBlockIds.hasNext()){
				String storageBlockId = curStorageBlockIds.next();
				ObjectNode storageBlockJson = this.coreApiClientService.storageBlockGet(this.kcClientAuthService.getAuthString(), storageBlockId).await().indefinitely();
				ItemSearchResults.StorageResult storageResult = new ItemSearchResults.StorageResult(storageBlockId, storageBlockJson.get("labelText").asText());
				Optional<MssModule> inModuleResult = this.moduleMaster.getModuleWithStorageBlock(storageBlockId);
				
				if(inModuleResult.isPresent()){
					ModuleInfo moduleInfo = inModuleResult.get().getModuleInfo();
					Integer moduleBlockNum = moduleInfo.getBlockNumForStorageBlockId(storageBlockId);
					
					if(!output.getWithModuleBlocks().containsKey(moduleInfo.getSerialId())){
						output.getWithModuleBlocks().put(
							moduleInfo.getSerialId(),
							new ItemSearchResults.ModuleResult(
								moduleInfo.getAssociatedStorageBlockId(),
								this.coreApiClientService.storageBlockGet(this.kcClientAuthService.getAuthString(), moduleInfo.getAssociatedStorageBlockId()).await().indefinitely()
									.get("labelText").asText()
							)
						);
					}
					
					if(!output.getWithModuleBlocks().get(moduleInfo.getSerialId()).getBlockToStorageMap().containsKey(moduleBlockNum)){
						output.getWithModuleBlocks().get(moduleInfo.getSerialId()).getBlockToStorageMap().put(
							moduleBlockNum,
							storageResult
						);
					}
				} else {
					output.getWithoutModuleBlocks().add(storageResult);
				}
			}
		}
		
		if(doHighlight) {
			this.moduleMaster.highlightResults(output.getWithModuleBlocks());
		}
		
		return output;
	}
}
