package com.ebp.openQuarterMaster.plugin.moduleInteraction;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Is the main bean that contains and handles modules.
 */
@ApplicationScoped
public class ModuleMaster {
	
	@Getter(AccessLevel.PRIVATE)
	private Map<String, MssModule> moduleMap = new HashMap<>();
	
	
	private void addModule(MssModule module){
		//TODO:: check if module already exists
		this.moduleMap.put(module.getModuleInfo().getSerialId(), module);
	}
	
	public ModuleMaster(){
		//TODO:: populate modules
	}
	
	//TODO:: scheduled method to process updates
}
