package com.ebp.openQuarterMaster.plugin.moduleInteraction.service;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.module.MssModule;
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
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

import java.util.List;
import java.util.Optional;

/**
 *
 *
 *
 *
 *
 *
 */
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
	OqmCoreApiClientService coreApiClient;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	KcClientAuthService kcClientAuthService;
	
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
	
//	public void ensureModuleBlocksExist(String oqmDbIdOrName, MssModule module) {
//		log.info(
//			"Ensuring module {} exists as a block with all {} blocks under it.",
//			module.getModuleInfo().getSerialId(),
//			module.getModuleInfo().getNumBlocks()
//		);
//		ObjectNode result = this.getCoreApiClient().storageBlockSearch(
//			this.getKcClientAuthService().getAuthString(),
//			oqmDbIdOrName,
//			StorageBlockSearch.builder()
//				.keywords(List.of(StorageBlockInteractionService.MSS_MODULE_KEYWORD))
//				.attributeKeys(List.of(StorageBlockInteractionService.MSS_MODULE_ID_ATT_KEY))
//				.attributeValues(List.of(module.getModuleInfo().getSerialId()))
//				.build()
//																		 ).await().indefinitely();
//		if (result.get("empty").asBoolean()) {
//			log.debug("Module {} did not exist yet. Creating.", module.getModuleInfo().getSerialId());
//			module.getModuleInfo().setAssociatedStorageBlockId(
//				this.getCoreApiClient().storageBlockAdd(this.getKcClientAuthService().getAuthString(), oqmDbIdOrName, this.getNewModuleStorageBlockJson(module))
//					.await().indefinitely()
//					.replaceAll("\"", "")
//
////				this.getBaseStationStorageRestClient().postNewStorageBlock(
////						this.getNewModuleStorageBlockJson(module)
////					)
////					.replaceAll("\"", "")
//			);
//			log.info(
//				"Module {} created, new id: {}",
//				module.getModuleInfo().getSerialId(),
//				module.getModuleInfo().getAssociatedStorageBlockId()
//			);
//		} else {
//			if (result.get("numResults").asInt() > 1) {
//				log.warn(
//					"Module {} search returned multiple results. Only counting the first one.",
//					module.getModuleInfo().getSerialId()
//				);
//			}
//			module.getModuleInfo().setAssociatedStorageBlockId(result.get("results").get(0).get("id").asText());
//
//			log.info(
//				"Module {} already existed with id {}",
//				module.getModuleInfo().getSerialId(),
//				module.getModuleInfo().getAssociatedStorageBlockId()
//			);
//		}
//
//		for (int curBlockNum = 1; curBlockNum <= module.getModuleInfo().getNumBlocks(); curBlockNum++) {
//			log.info(
//				"Ensuring module block {}[{}] exists as a block.",
//				module.getModuleInfo().getSerialId(),
//				curBlockNum
//			);
//			result = this.getCoreApiClient().storageBlockSearch(
//				this.getKcClientAuthService().getAuthString(),
//				oqmDbIdOrName,
//				StorageBlockSearch.builder()
//					.keywords(List.of(StorageBlockInteractionService.MSS_MODULE_BLOCK_KEYWORD))
//					.attributeKeys(List.of(
//						StorageBlockInteractionService.MSS_MODULE_ID_ATT_KEY,
//						StorageBlockInteractionService.MSS_MODULE_BLOCK_NUM_ATT_KEY
//					))
//					.attributeValues(List.of(
//						module.getModuleInfo().getSerialId(),
//						""+curBlockNum
//					))
//					.build()
//			).await().indefinitely();
//			String curId;
//			if (result.get("empty").asBoolean()) {
//				log.debug("Module block {}[{}] did not exist yet. Creating.", module.getModuleInfo().getSerialId(), curBlockNum);
//				curId = this.getCoreApiClient().storageBlockAdd(this.getKcClientAuthService().getAuthString(), oqmDbIdOrName, this.getNewModuleBlockStorageBlockJson(module, curBlockNum))
//							.await().indefinitely();
////
////					this.getBaseStationStorageRestClient().postNewStorageBlock(
////
////					this.getNewModuleBlockStorageBlockJson(module, curBlockNum)
////				);
//				log.info(
//					"Module block {}[{}] created, new id: {}",
//					module.getModuleInfo().getSerialId(),
//					curBlockNum,
//					curId
//				);
//			} else {
//				if (result.get("numResults").asInt() > 1) {
//					log.warn(
//						"Module block {}[{}] search returned multiple results. Only counting the first one.",
//						module.getModuleInfo().getSerialId(),
//						curBlockNum
//					);
//				}
//
//				curId = result.get("results").get(0).get("id").asText();
//
//				log.info(
//					"Module block {}[{}] already existed with id {}",
//					module.getModuleInfo().getSerialId(),
//					curBlockNum,
//					curId
//				);
//			}
//			curId = curId.replaceAll("\"", "");
//			log.debug("Got storage block id {} for module block {}[{}]", curId, module.getModuleInfo().getSerialId(), curBlockNum);
//			module.getModuleInfo().getStorageBlockToModBlockNums().put(curId, curBlockNum);
//		}
//		log.info(
//			"Module {} exists as a block with all {} blocks under it.",
//			module.getModuleInfo().getSerialId(),
//			module.getModuleInfo().getNumBlocks()
//		);
//	}


	public Optional<ObjectNode> getStorageBlockForModule(String oqmDbId, String moduleId) {
		log.info("Getting storage block for module {} from db {}", moduleId, oqmDbId);

		ObjectNode searchResult = this.getCoreApiClient().storageBlockSearch(
			this.getKcClientAuthService().getAuthString(), oqmDbId,
			StorageBlockSearch.builder()
				.keywords(List.of(StorageBlockInteractionService.MSS_MODULE_KEYWORD))
				.attributeKeys(List.of(MSS_MODULE_ID_ATT_KEY))
				.attributeValues(List.of(moduleId))
				.build()
		).await().indefinitely();

		if(searchResult.get("empty").asBoolean()){
			return Optional.empty();
		}

		if(searchResult.get("numResults").asInt() > 1){
			throw new IllegalStateException("Cannot have more than one storage block representing a single module as a whole.");
		}

		return Optional.of((ObjectNode) searchResult.get("results").get(0));
	}

	public Optional<ObjectNode> getStorageBlockForModuleBlock(String oqmDbId, String moduleId, int blockNum){
		log.info("Getting storage block for module block {} in module {} from db {}", blockNum, moduleId, oqmDbId);

		ObjectNode searchResult = this.getCoreApiClient().storageBlockSearch(
			this.getKcClientAuthService().getAuthString(), oqmDbId,
			StorageBlockSearch.builder()
				.keywords(List.of(StorageBlockInteractionService.MSS_MODULE_BLOCK_KEYWORD))
				.attributeKeys(List.of(MSS_MODULE_ID_ATT_KEY, MSS_MODULE_BLOCK_NUM_ATT_KEY))
				.attributeValues(List.of(moduleId, ""+blockNum))
				.build()
		).await().indefinitely();

		if(searchResult.get("empty").asBoolean()){
			return Optional.empty();
		}

		if(searchResult.get("numResults").asInt() > 1){
			throw new IllegalStateException("Cannot have more than one storage block representing a single module block.");
		}

		return Optional.of((ObjectNode) searchResult.get("results").get(0));
	}


}
