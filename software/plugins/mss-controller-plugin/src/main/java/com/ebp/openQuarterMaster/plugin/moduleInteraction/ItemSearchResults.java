package com.ebp.openQuarterMaster.plugin.moduleInteraction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemSearchResults {
	private Map<String, ModuleResult> withModuleBlocks = new HashMap<>();
	private Set<StorageResult> withoutModuleBlocks = new HashSet<>();
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ModuleResult {
		private Map<Integer, Set<StorageResult>> blockToStorageMap = new HashMap<>();
	}
	
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class StorageResult {
		private String id;
		private String labelText;
	}
}
