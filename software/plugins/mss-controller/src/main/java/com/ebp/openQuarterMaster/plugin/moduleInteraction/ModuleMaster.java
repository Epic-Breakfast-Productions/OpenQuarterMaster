package com.ebp.openQuarterMaster.plugin.moduleInteraction;

import com.ebp.openQuarterMaster.plugin.config.ModuleConfig;
import com.ebp.openQuarterMaster.plugin.model.module.command.HighlightBlocksCommand;
import com.ebp.openQuarterMaster.plugin.model.module.command.response.ModuleInfo;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.module.serialModule.MssSerialModule;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.module.MssModule;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.service.StorageBlockInteractionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Is the main bean that contains and handles modules.
 */
@Slf4j
@ApplicationScoped
public class ModuleMaster {
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	ObjectMapper objectMapper;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	ModuleConfig moduleConfig;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	StorageBlockInteractionService storageBlockInteractionService;
	
	/**
	 * Serial id -> Module map.
	 *
	 * Is the master holder of modules
	 */
	@Getter(AccessLevel.PRIVATE)
	private final Map<String, MssModule> moduleMap = new HashMap<>();
	
	/**
	 * Storage block id (from Base station) -> Module map
	 *
	 * Calculated to make this conversion easier.
	 */
	@Getter(AccessLevel.PRIVATE)
	private final Map<String, MssModule> storageToModuleMap = new TreeMap<>();
	
	@PostConstruct
	void init(){
		this.populateModules();
		this.ensureModuleStorageBlocksExist();
		this.populateStorageToModuleMap();
	}
	
//	private void populateStorageToModuleMap(){
//		log.info("Populating cache of Storage Block Ids to their modules.");
//		for(MssModule curModule : this.getModules()){
//			ModuleInfo curModuleInfo = curModule.getModuleInfo();
//
//			for(String curStorageBlockId : curModuleInfo.getStorageBlockToModBlockNums().keySet()){
//				this.getStorageToModuleMap().put(curStorageBlockId, curModule);
//			}
//		}
//		log.info("Done populating cache of Storage Block Ids to their modules.");
//		log.debug("Number of storage blocks tracked: {}", this.getStorageToModuleMap().size());
//	}
	
	private void ensureModuleStorageBlocksExist(){
		for(MssModule curModule : this.getModules()){
			this.getStorageBlockInteractionService().ensureModuleBlocksExist(curModule);
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
	
	private void populateModules(){
		if(this.getModuleConfig().serial().scanSerial()){
			//TODO:: scan over serial ports for valid modules. How to do properly?
		}
		
		for(ModuleConfig.SerialConfig.SerialModuleConfig serialModuleConfig : this.getModuleConfig().serial().modules()){
			log.info("Adding MSS module over Serial. Port: {}", serialModuleConfig.portPath());
			
			MssSerialModule newModule = new MssSerialModule(
				this.getObjectMapper(),
				serialModuleConfig.portPath(),
				serialModuleConfig.baudRate()
			);
			this.addModule(newModule);
			
			log.info("Added MSS module over Serial. Port: {} Id: {}", serialModuleConfig.portPath(), newModule.getModuleInfo().getSerialId());
		}
	}
	
	public Set<String> getModuleIds(){
		return this.getModuleMap().keySet();
	}
	
	public Collection<MssModule> getModules(){
		return this.getModuleMap().values();
	}
	
	public MssModule getModule(String moduleId){
		return this.getModuleMap().get(moduleId);
	}
	
	public ModuleInfo getModuleInfo(String moduleId){
		return this.getModule(moduleId).getModuleInfo();
	}
	
	public Optional<MssModule> getModuleWithStorageBlock(String storageBlockId){
		return Optional.ofNullable(this.getStorageToModuleMap().get(storageBlockId));
	}
	
	public void highlightResults(Map<String, ItemSearchResults.ModuleResult> resultsToHighlight){
		
		for (Map.Entry<String, ItemSearchResults.ModuleResult> resultEntry : resultsToHighlight.entrySet()){
			MssModule curModule = this.getModule(resultEntry.getKey());
			
			HighlightBlocksCommand command = new HighlightBlocksCommand(
				"RAND",
				resultEntry.getValue()
					.getBlockToStorageMap().keySet()
			);
			
			curModule.sendBlockHighlightCommand(command);
		}
	}
	
	@Scheduled(every = "P2D")
	public void processUpdates(){
		log.info("Processing updates.");
		//TODO:: this
	}
}
