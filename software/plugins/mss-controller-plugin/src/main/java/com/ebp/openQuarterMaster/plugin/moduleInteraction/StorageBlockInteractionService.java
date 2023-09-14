package com.ebp.openQuarterMaster.plugin.moduleInteraction;

import com.ebp.openQuarterMaster.plugin.restClients.BaseStationStorageBlockRestClient;
import com.ebp.openQuarterMaster.plugin.restClients.StorageBlockSearch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class StorageBlockInteractionService {
	
	public static final String MSS_MODULE_KEYWORD = "mss/module";
	public static final String MSS_MODULE_ID_ATT_KEY = "mss/moduleId";
	public static final String MSS_MODULE_BLOCK_KEYWORD = "mss/module/block";
	public static final String MSS_MODULE_BLOCK_NUM_ATT_KEY = "mss/moduleBlockNum";
	
	public static String ordinal(int i) {
		String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
		switch (i % 100) {
			case 11:
			case 12:
			case 13:
				return i + "th";
			default:
				return i + suffixes[i % 10];
			
		}
	}
	
	@Getter
	@Inject
	ObjectMapper objectMapper;
	
	@RestClient
	@Getter(AccessLevel.PRIVATE)
	BaseStationStorageBlockRestClient baseStationStorageRestClient;
	
	private ObjectNode getNewStorageBlockJson() {
		try {
			return (ObjectNode) this.objectMapper.readTree("""
					{
						"attributes": {},
						"keywords": [],
						"label": "",
						"description": "string"
					}
				""");
		} catch(JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private ObjectNode getNewModuleStorageBlockJson(MssModule module) {
		ObjectNode newJson = this.getNewStorageBlockJson();
		
		((ArrayNode) newJson.get("keywords")).add(MSS_MODULE_KEYWORD);
		((ObjectNode) newJson.get("attributes")).put(MSS_MODULE_ID_ATT_KEY, module.getModuleInfo().getSerialId());
		newJson.put("label", "MSS Module " + module.getModuleInfo().getSerialId());
		newJson.put("description", "MSS Module " + module.getModuleInfo().getSerialId() + ". Added by the MSS Controller plugin.");
		
		return newJson;
	}
	
	private ObjectNode getNewModuleBlockStorageBlockJson(
		MssModule module,
		int blockNum
	) {
		ObjectNode newJson = this.getNewStorageBlockJson();
		
		((ArrayNode) newJson.get("keywords")).add(MSS_MODULE_BLOCK_KEYWORD);
		((ObjectNode) newJson.get("attributes")).put(MSS_MODULE_ID_ATT_KEY, module.getModuleInfo().getSerialId());
		((ObjectNode) newJson.get("attributes")).put(MSS_MODULE_BLOCK_NUM_ATT_KEY, blockNum);
		newJson.put("label", "MSS Module " + module.getModuleInfo().getSerialId() + "[" + blockNum + "]");
		newJson.put("parent", module.getModuleInfo().getAssociatedStorageBlockId());
		newJson.put("description", ordinal(blockNum) + " block under the MSS Module " + module.getModuleInfo().getSerialId() + ". Added by the MSS Controller plugin.");
		
		return newJson;
	}
	
	public void ensureModuleBlocksExist(MssModule module) {
		log.info(
			"Ensuring module {} exists as a block with all {} blocks under it.",
			module.getModuleInfo().getSerialId(),
			module.getModuleInfo().getNumBlocks()
		);
		ArrayNode result = this.getBaseStationStorageRestClient().searchBlocks(new StorageBlockSearch(module));
		
		if (result.isEmpty()) {
			log.debug("Module {} did not exist yet. Creating.", module.getModuleInfo().getSerialId());
			module.getModuleInfo().setAssociatedStorageBlockId(
				this.getBaseStationStorageRestClient().postNewStorageBlock(
						this.getNewModuleStorageBlockJson(module)
					)
					.replaceAll("\"", "")
			);
			log.info(
				"Module {} created, new id: {}",
				module.getModuleInfo().getSerialId(),
				module.getModuleInfo().getAssociatedStorageBlockId()
			);
		} else {
			if (result.size() > 1) {
				log.warn(
					"Module {} search returned multiple results. Only counting the first one.",
					module.getModuleInfo().getSerialId()
				);
			}
			module.getModuleInfo().setAssociatedStorageBlockId(result.get(0).get("id").asText());
			
			log.info(
				"Module {} already existed with id {}",
				module.getModuleInfo().getSerialId(),
				module.getModuleInfo().getAssociatedStorageBlockId()
			);
		}
		
		for (int curBlockNum = 1; curBlockNum <= module.getModuleInfo().getNumBlocks(); curBlockNum++) {
			log.info(
				"Ensuring module block {}[{}] exists as a block.",
				module.getModuleInfo().getSerialId(),
				curBlockNum,
				module.getModuleInfo().getNumBlocks()
			);
			result = this.getBaseStationStorageRestClient().searchBlocks(new StorageBlockSearch(module, curBlockNum));
			
			if (result.isEmpty()) {
				log.debug("Module block {}[{}] did not exist yet. Creating.", module.getModuleInfo().getSerialId(), curBlockNum);
				String newId = this.getBaseStationStorageRestClient().postNewStorageBlock(
					this.getNewModuleBlockStorageBlockJson(module, curBlockNum)
				);
				log.info(
					"Module block {}[{}] created, new id: {}",
					module.getModuleInfo().getSerialId(),
					curBlockNum,
					newId
				);
			} else {
				if (result.size() > 1) {
					log.warn(
						"Module block {}[{}] search returned multiple results. Only counting the first one.",
						module.getModuleInfo().getSerialId(),
						curBlockNum
					);
				}
				
				log.info(
					"Module block {}[{}] already existed with id {}",
					module.getModuleInfo().getSerialId(),
					curBlockNum,
					module.getModuleInfo().getAssociatedStorageBlockId()
				);
			}
		}
		log.info(
			"Module {} exists as a block with all {} blocks under it.",
			module.getModuleInfo().getSerialId(),
			module.getModuleInfo().getNumBlocks()
		);
	}
}
