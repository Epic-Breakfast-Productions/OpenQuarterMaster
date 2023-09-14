package com.ebp.openQuarterMaster.plugin.moduleInteraction.command.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleInfo {
	
	private String specVersion;
	private String serialId;
	private String manufactureDate;//TODO:: do date appropriately?
	private int numBlocks;
	private String associatedStorageBlockId;
	private Map<String, Integer> storageBlockToModBlockNums = new TreeMap<>();
	//TODO:: capabilities
	
	public String getStorageBlockIdForBlock(int blockNum) {
		return storageBlockToModBlockNums.entrySet()
				   .stream()
				   .filter(entry->blockNum == entry.getValue())
				   .map(Map.Entry::getKey)
				   .findFirst().get();
	}
	
	public Stream<Integer> getBlockNumStream(){
		return IntStream.range(1, this.getNumBlocks() + 1).boxed();
	}
}
