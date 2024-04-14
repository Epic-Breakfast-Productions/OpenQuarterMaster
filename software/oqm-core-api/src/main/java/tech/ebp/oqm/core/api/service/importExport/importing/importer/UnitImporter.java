package tech.ebp.oqm.core.api.service.importExport.importing.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.units.CustomUnitEntry;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.rest.search.CustomUnitSearch;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.mongo.CustomUnitService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class UnitImporter extends TopLevelImporter<Long> {

	@Getter(AccessLevel.PRIVATE)
	@Inject
	CustomUnitService customUnitService;
	
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
			newId = this.getCustomUnitService().add(clientSession, curObj, importingEntity);
		} catch(Throwable e) {
			log.error("Failed to import object: ", e);
			throw e;
		}
		log.info("Read in object. new id == old? {}", newId.equals(oldId));
		assert newId.equals(oldId); //TODO:: better check?
		
		UnitUtils.registerAllUnits(curObj);
	}

	@Override
	public Path getObjectDirPath(Path topLevelPath) {
		return topLevelPath.resolve(this.customUnitService.getCollectionName());
	}

	@Override
	public Long readInObjectsImpl(
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity,
		DataImportOptions options
	) throws IOException {

		List<File> filesForObject = getObjectFiles(directory);
		
		log.info("Found {} files for {} in {}", filesForObject.size(), this.getCustomUnitService().getCollectionName(), directory);
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
			this.customUnitService.getCollectionName(),
			sw
		);
		
		return (long)filesForObject.size();
	}
}
