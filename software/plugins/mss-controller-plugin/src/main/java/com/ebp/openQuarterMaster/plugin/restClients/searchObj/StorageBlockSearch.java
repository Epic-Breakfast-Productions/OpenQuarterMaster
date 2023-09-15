package com.ebp.openQuarterMaster.plugin.restClients.searchObj;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.MssModule;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.service.StorageBlockInteractionService;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageBlockSearch {
	@QueryParam("keyword")
	List<String> keywords;
	
	@QueryParam("attributeKey") List<String> attributeKeys;
	@QueryParam("attributeValue") List<String> attributeValues;
	
	public StorageBlockSearch(
		MssModule module
	){
		this.keywords = List.of(StorageBlockInteractionService.MSS_MODULE_KEYWORD);
		this.attributeKeys = List.of(StorageBlockInteractionService.MSS_MODULE_ID_ATT_KEY);
		this.attributeValues = List.of(module.getModuleInfo().getSerialId());
	}
	
	public StorageBlockSearch(
		MssModule module,
		int blockNum
	){
		this.keywords = List.of(StorageBlockInteractionService.MSS_MODULE_BLOCK_KEYWORD);
		this.attributeKeys = List.of(
			StorageBlockInteractionService.MSS_MODULE_ID_ATT_KEY,
			StorageBlockInteractionService.MSS_MODULE_BLOCK_NUM_ATT_KEY
		);
		this.attributeValues = List.of(
			module.getModuleInfo().getSerialId(),
			""+blockNum
		);
	}
}
