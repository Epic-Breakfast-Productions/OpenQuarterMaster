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
	private Map<Integer, String> storageBlockToModBlockNums = new TreeMap<>();

	public String getStorageBlockIdForBlock(int blockNum) {
		return this.getStorageBlockToModBlockNums().get(blockNum);
	}

	public Integer getBlockNumForStorageBlockId(String storageBlockId){
		return storageBlockToModBlockNums.entrySet()
			.stream()
			.filter(entry->entry.getValue().equals(storageBlockId))
			.map(Map.Entry::getKey)
			.findFirst().get();
	}
}
