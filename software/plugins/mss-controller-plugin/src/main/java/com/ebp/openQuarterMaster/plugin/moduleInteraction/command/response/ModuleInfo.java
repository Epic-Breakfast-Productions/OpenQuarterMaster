package com.ebp.openQuarterMaster.plugin.moduleInteraction.command.response;

import io.quarkus.arc.All;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleInfo {
	private String specVersion;
	private String serialId;
	private String manufactureDate;//TODO:: do date appropriately?
	private int numBlocks;
	//TODO:: capabilities
}
