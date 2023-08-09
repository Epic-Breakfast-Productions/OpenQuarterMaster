package tech.ebp.oqm.baseStation.service.importExport.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.rest.search.CustomUnitSearch;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.units.CustomUnitEntry;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class UnitImporter extends ObjectImporter<CustomUnitEntry, CustomUnitSearch, CustomUnitService> {
	
	@Inject
	public UnitImporter(CustomUnitService mongoService) {
		super(mongoService);
	}
	
	private static boolean isUnitNotFoundJsonException(JsonProcessingException e){
		return e.getMessage().matches("^Unit string given \\((.*)\\) does not represent any of the possible valid units.(.*)$");
	}
	
	private void readInObject(
		ClientSession clientSession,
		CustomUnitEntry curObj,
		InteractingEntity importingEntity
	) {
		ObjectId oldId = curObj.getId();
		ObjectId newId;
		try {
			newId = this.getObjectService().add(clientSession, curObj, importingEntity);
		} catch(Throwable e) {
			log.error("Failed to import object: ", e);
			throw e;
		}
		log.info("Read in object. new id == old? {}", newId.equals(oldId));
		assert newId.equals(oldId); //TODO:: better check?
		
		UnitUtils.registerAllUnits(curObj);
	}
	
	private void readInObject(
		ClientSession clientSession,
		File curFile,
		InteractingEntity importingEntity,
		Map<String, List<ObjectNode>> needParentMap
	) throws IOException {
		CustomUnitEntry curObj;
		
		try {
			curObj = ObjectUtils.OBJECT_MAPPER.readValue(curFile, CustomUnitEntry.class);
		} catch(JsonProcessingException e) {
			
			if (isUnitNotFoundJsonException(e)) {
				log.info("Got derived unit before parent: {}", e.getMessage());
				ObjectNode curUnitEntryJson = (ObjectNode) ObjectUtils.OBJECT_MAPPER.readTree(curFile);
				
				needParentMap.computeIfAbsent(
								 curUnitEntryJson.get("unitCreator").get("symbol").asText(),
								 k->new ArrayList<>()
							 )
							 .add(curUnitEntryJson);
				return;
			}
			
			throw e;
		}
		
		
		try {
			this.readInObject(
				clientSession,
				curObj,
				importingEntity
			);
		} catch(Throwable e) {
			log.error("Failed to process object file {}: ", curFile, e);
			throw e;
		}
	}
	
	@Override
	public long readInObjects(
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity
	) throws IOException {
		Path objectDirPath = this.getObjDirPath(directory);
		List<File> filesForObject = getObjectFiles(objectDirPath);
		
		log.info("Found {} files for {} in {}", filesForObject.size(), this.getObjectService().getCollectionName(), objectDirPath);
		Map<String, List<ObjectNode>> needParentMap = new HashMap<>();
		StopWatch sw = StopWatch.createStarted();
		for (File curObjFile : filesForObject) {
			this.readInObject(clientSession, curObjFile, importingEntity, needParentMap);
		}
		
		while(!needParentMap.isEmpty()){
			Map<String, List<ObjectNode>> newNeedParentMap = new HashMap<>();
			
			List<String> keys = new ArrayList<>(needParentMap.keySet());
			
			for(String curKey : keys){
				List<ObjectNode> curUnitEntryList = needParentMap.remove(curKey);
				
				for(ObjectNode curUnitEntryJson : curUnitEntryList){
					CustomUnitEntry curEntry;
					try{
						curEntry = ObjectUtils.OBJECT_MAPPER.treeToValue(curUnitEntryJson, CustomUnitEntry.class);
					} catch(JsonProcessingException e){
						if(isUnitNotFoundJsonException(e)){
							newNeedParentMap.put(curKey, curUnitEntryList);
							break;
						} else {
							throw e;
						}
					}
					this.readInObject(
						clientSession,
						curEntry,
						importingEntity
					);
				}
			}
			
			needParentMap = newNeedParentMap;
		}
		
		
		
		
		sw.stop();
		log.info(
			"Read in {} {} objects in {}",
			filesForObject.size(),
			this.getObjectService().getCollectionName(),
			sw
		);
		
		return filesForObject.size();
	}
	
	
}
