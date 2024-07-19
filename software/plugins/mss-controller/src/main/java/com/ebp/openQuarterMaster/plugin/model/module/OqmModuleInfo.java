package com.ebp.openQuarterMaster.plugin.model.module;

import com.ebp.openQuarterMaster.plugin.model.module.command.response.ModuleInfo;
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
public class OqmModuleInfo {

	private ModuleInfo moduleInfo;
	private Map<String, ModuleOqmDbInfo> associatedStorageBlockIds;

}
