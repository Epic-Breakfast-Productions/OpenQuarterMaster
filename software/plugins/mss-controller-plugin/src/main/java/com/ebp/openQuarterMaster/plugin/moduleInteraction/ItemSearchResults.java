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
	
	/**
	 * Module Serial Id -> module result
	 */
	private Map<String, ModuleResult> withModuleBlocks = new HashMap<>();
	private Set<StorageResult> withoutModuleBlocks = new HashSet<>();
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ModuleResult {
		private String moduleStorageBlockId;
		private String moduleStorageBlockIdLabelText;
		//TODO:: enhance to include item infos
		private Map<Integer, StorageResult> blockToStorageMap = new HashMap<>();
		
		public ModuleResult(String moduleStorageBlockId, String moduleStorageBlockIdLabelText){
			this.moduleStorageBlockId = moduleStorageBlockId;
			this.moduleStorageBlockIdLabelText = moduleStorageBlockIdLabelText;
		}
	}
	
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class StorageResult {
		private String id;
		private String labelText;
	}
}
