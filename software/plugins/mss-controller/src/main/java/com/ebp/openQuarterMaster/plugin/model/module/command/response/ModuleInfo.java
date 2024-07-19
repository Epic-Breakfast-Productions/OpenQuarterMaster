package com.ebp.openQuarterMaster.plugin.model.module.command.response;

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
	//TODO:: capabilities

	public Stream<Integer> getBlockNumStream(){
		return IntStream.range(1, this.getNumBlocks() + 1).boxed();
	}
}
