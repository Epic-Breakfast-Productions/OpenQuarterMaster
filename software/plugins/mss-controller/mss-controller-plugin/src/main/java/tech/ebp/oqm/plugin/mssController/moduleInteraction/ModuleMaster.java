package tech.ebp.oqm.plugin.mssController.moduleInteraction;

import jakarta.inject.Named;
import tech.ebp.oqm.plugin.mssController.config.ModuleConfig;
import tech.ebp.oqm.plugin.mssController.model.module.ModuleOqmDbInfo;
import tech.ebp.oqm.plugin.mssController.model.module.OqmModuleInfo;
import tech.ebp.oqm.plugin.mssController.lib.command.HighlightBlocksCommand;
import tech.ebp.oqm.plugin.mssController.moduleInteraction.module.serialModule.MssSerialModule;
import tech.ebp.oqm.plugin.mssController.moduleInteraction.module.MssModule;
import tech.ebp.oqm.plugin.mssController.moduleInteraction.service.StorageBlockInteractionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.OqmDatabaseService;

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
@Named("ModuleMaster")
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

	@Inject
	@Getter(AccessLevel.PRIVATE)
	OqmDatabaseService oqmDatabaseService;
	
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
		this.discoverModules();
		this.populateModuleStorageBlocksFromDb();
		if(this.getModuleConfig().autoCreateStorageBlocksInAllDbs()){
			//TODO:: this
//			this.ensureModuleStorageBlocksExist();
		}
		this.populateStorageToModuleMap();
	}
	
	private void populateStorageToModuleMap(){
		log.info("Populating cache of Storage Block Ids to their modules.");
		for(MssModule curModule : this.getModules()){
			OqmModuleInfo curModuleInfo = curModule.getModuleInfo();

			for(String curStorageBlockId : curModuleInfo.getAssociatedStorageBlockIds().keySet()){
				this.getStorageToModuleMap().put(curStorageBlockId, curModule);
			}
		}
		log.info("Done populating cache of Storage Block Ids to their modules.");
		log.debug("Number of storage blocks tracked: {}", this.getStorageToModuleMap().size());
	}
	
//	private void ensureModuleStorageBlocksExist(){
//		for(MssModule curModule : this.getModules()){
//			this.getStorageBlockInteractionService().ensureModuleBlocksExist(curModule);
//		}
//	}

	private void populateModuleStorageBlocksFromDb(){
		log.info("Populating module storage blocks from database.");
		for(JsonNode curDb : this.getOqmDatabaseService().getDatabases()){
			String curDbId = curDb.get("id").asText();

			for(MssModule curModule : this.getModules()){
				Optional<ObjectNode> result = this.getStorageBlockInteractionService().getStorageBlockForModule(curDbId, curModule.getModuleSerialId());

				if(result.isEmpty()){
					log.info("No storage block found for module {} in db {}", curModule.getModuleSerialId(), curDbId);
					continue;
				}
				String moduleStorageBlockId = curDb.get("id").asText();

				ModuleOqmDbInfo.ModuleOqmDbInfoBuilder builder = ModuleOqmDbInfo.builder()
					.associatedStorageBlockId(moduleStorageBlockId);
				Map<Integer, String> blockNumToStorageIdsMap = new TreeMap<>();

				for(Integer curModuleBlockNum : curModule.getModuleInfo().getModuleInfo().getBlockNumStream().toList()){
					Optional<ObjectNode> storageBlockForModuleBlockNum = this.getStorageBlockInteractionService().getStorageBlockForModuleBlock(curDbId, moduleStorageBlockId, curModuleBlockNum);

					if(storageBlockForModuleBlockNum.isEmpty()){
						throw new IllegalStateException("Cannot not have a storage block for any given module block.");
					}

					blockNumToStorageIdsMap.put(
						curModuleBlockNum,
						storageBlockForModuleBlockNum.get().get("id").asText()
					);
				}
			}
		}
	}

	/**
	 * Adds a module to the set
	 * @param module
	 */
	private void addModule(MssModule module){
		log.info("Adding module {}", module.getModuleInfo().getModuleInfo().getSerialId());
		if(this.getModuleMap().containsKey(module.getModuleInfo().getModuleInfo().getSerialId())){
			log.warn(
				"Already have module with SerialId {} over {}. Attempted to add another over {}. Discarding duplicate.",
				module.getModuleInfo().getModuleInfo().getSerialId(),
				this.moduleMap.get(module.getModuleInfo().getModuleInfo().getSerialId()).getClass().getSimpleName(),
				module.getClass().getSimpleName()
			);
			return;
		}
		
		this.moduleMap.put(module.getModuleInfo().getModuleInfo().getSerialId(), module);
	}

	/**
	 *
	 */
	private void discoverModules(){
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
			
			log.info("Added MSS module over Serial. Port: {} Id: {}", serialModuleConfig.portPath(), newModule.getModuleInfo().getModuleInfo().getSerialId());
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
	
	public OqmModuleInfo getModuleInfo(String moduleId){
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
