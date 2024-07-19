package com.ebp.openQuarterMaster.plugin.model.module;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.TreeMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleOqmDbInfo {

	private String associatedStorageBlockId;
	private Map<String, Integer> storageBlockToModBlockNums = new TreeMap<>();

	public String getStorageBlockIdForBlock(int blockNum) {
		return storageBlockToModBlockNums.entrySet()
			.stream()
			.filter(entry->blockNum == entry.getValue())
			.map(Map.Entry::getKey)
			.findFirst().get();
	}

	public Integer getBlockNumForStorageBlockId(String storageBlockId){
		return this.getStorageBlockToModBlockNums().get(storageBlockId);
	}
}
