package com.ebp.openQuarterMaster.plugin.moduleInteraction;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.response.ModuleInfo;
import com.ebp.openQuarterMaster.plugin.restClients.BaseStationInventoryItemRestClient;
import com.ebp.openQuarterMaster.plugin.restClients.BaseStationStorageBlockRestClient;
import com.ebp.openQuarterMaster.plugin.restClients.searchObj.InventoryItemSearch;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class ItemSearchService {

	@RestClient
	BaseStationInventoryItemRestClient inventoryItemRestClient;
	
	@RestClient
	BaseStationStorageBlockRestClient storageBlockRestClient;
	
	@Inject
	ModuleMaster moduleMaster;
	
	/**
	 * TODO:: logic of what storage blocks are in an item search should be in BaseStation
	 * @param itemSearch
	 * @param doHighlight
	 * @return
	 */
	public ItemSearchResults searchForItemLocations(InventoryItemSearch itemSearch, boolean doHighlight){
		ArrayNode itemResultsJson = this.inventoryItemRestClient.searchItems(itemSearch);
		ItemSearchResults output = new ItemSearchResults();
		
		for(JsonNode curItemJson : itemResultsJson){
			Iterator<String> curStorageBlockIds = curItemJson.get("storageMap").fieldNames();
			
			while (curStorageBlockIds.hasNext()){
				String storageBlockId = curStorageBlockIds.next();
				ObjectNode storageBlockJson = this.storageBlockRestClient.getBlock(storageBlockId);
				ItemSearchResults.StorageResult storageResult = new ItemSearchResults.StorageResult(storageBlockId, storageBlockJson.get("labelText").asText());
				Optional<MssModule> inModuleResult = this.moduleMaster.getModuleWithStorageBlock(storageBlockId);
				
				if(inModuleResult.isPresent()){
					ModuleInfo moduleInfo = inModuleResult.get().getModuleInfo();
					Integer moduleBlockNum = moduleInfo.getBlockNumForStorageBlockId(storageBlockId);
					
					if(!output.getWithModuleBlocks().containsKey(moduleInfo.getSerialId())){
						output.getWithModuleBlocks().put(moduleInfo.getSerialId(), new ItemSearchResults.ModuleResult());
					}
					
					if(!output.getWithModuleBlocks().get(moduleInfo.getSerialId()).getBlockToStorageMap().containsKey(moduleBlockNum)){
						output.getWithModuleBlocks().get(moduleInfo.getSerialId()).getBlockToStorageMap().put(
							moduleBlockNum,
							new HashSet<>()
						);
					}
					
					output.getWithModuleBlocks().get(moduleInfo.getSerialId()).getBlockToStorageMap().get(moduleBlockNum).add(
						storageResult
					);
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
