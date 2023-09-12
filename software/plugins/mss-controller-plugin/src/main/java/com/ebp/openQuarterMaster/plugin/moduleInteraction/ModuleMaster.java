package com.ebp.openQuarterMaster.plugin.moduleInteraction;

import com.ebp.openQuarterMaster.plugin.config.ModuleConfig;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.response.ModuleInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Is the main bean that contains and handles modules.
 */
@Slf4j
@ApplicationScoped
public class ModuleMaster {
	
	@Getter(AccessLevel.PRIVATE)
	private final ModuleConfig moduleConfig;
	
	@Getter(AccessLevel.PRIVATE)
	private final Map<String, MssModule> moduleMap = new HashMap<>();
	
	@Inject
	public ModuleMaster(
		ModuleConfig moduleConfig
	){
		this.moduleConfig = moduleConfig;
		
		if(this.getModuleConfig().serial().scanSerial()){
			//TODO:: scan over serial ports for valid modules
		}
		
		for(ModuleConfig.SerialConfig.SerialModuleConfig serialModuleConfig : this.getModuleConfig().serial().modules()){
			//TODO:: add
		}
	}
	
	private void addModule(MssModule module){
		log.info("Adding module {}", module.getModuleInfo().getSerialId());
		if(this.getModuleMap().containsKey(module.getModuleInfo().getSerialId())){
			log.warn(
				"Already have module with SerialId {} over {}. Attempted to add another over {}. Discarding duplicate.",
				module.getModuleInfo().getSerialId(),
				this.moduleMap.get(module.getModuleInfo().getSerialId()).getClass().getSimpleName(),
				module.getClass().getSimpleName()
			);
			return;
		}
		this.moduleMap.put(module.getModuleInfo().getSerialId(), module);
	}
	
	public Set<String> getModuleIds(){
		return this.getModuleMap().keySet();
	}
	
	public MssModule getModule(String moduleId){
		return this.getModuleMap().get(moduleId);
	}
	
	public ModuleInfo getModuleInfo(String moduleId){
		return this.getModule(moduleId).getModuleInfo();
	}
	
	
	//TODO:: scheduled method to process updates
}
