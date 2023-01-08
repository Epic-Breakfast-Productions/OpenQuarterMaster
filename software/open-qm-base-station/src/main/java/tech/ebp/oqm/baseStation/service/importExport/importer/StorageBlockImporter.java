package tech.ebp.oqm.baseStation.service.importExport.importer;

import com.mongodb.client.ClientSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.rest.search.StorageBlockSearch;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbModValidationException;
import tech.ebp.oqm.lib.core.Utils;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class StorageBlockImporter extends ObjectImporter<StorageBlock, StorageBlockSearch, StorageBlockService>{
	
	
	public StorageBlockImporter(StorageBlockService mongoService) {
		super(mongoService);
	}
	
	private void readInObject(
		ClientSession clientSession,
		StorageBlock curObj,
		InteractingEntity importingEntity,
		Map<ObjectId, List<StorageBlock>> needParentMap,
		List<ObjectId> addedList
	) {
		ObjectId oldId = curObj.getId();
		ObjectId newId;
		try {
			newId = this.getObjectService().add(clientSession, curObj, importingEntity);
		} catch(DbModValidationException e){
			if(e.getMessage().contains("No parent exists")){
				ObjectId curParent = ((StorageBlock)curObj).getParent();
				needParentMap.computeIfAbsent(curParent, k->new ArrayList<>()).add(curObj);
				return;
			}
			throw e;
		} catch(Throwable e) {
			log.error("Failed to import object: ", e);
			throw e;
		}
		log.info("Read in object. new id == old? {}", newId.equals(oldId));
		assert newId.equals(oldId); //TODO:: better check?
		addedList.add(oldId);
	}
	
	private void readInObject(
		ClientSession clientSession,
		File curFile,
		InteractingEntity importingEntity,
		Map<ObjectId, List<StorageBlock>> needParentMap,
		List<ObjectId> addedList
	) throws IOException {
		try {
			this.readInObject(
				clientSession,
				Utils.OBJECT_MAPPER.readValue(curFile, this.getObjectService().getClazz()),
				importingEntity,
				needParentMap,
				addedList
			);
		} catch(Throwable e){
			log.error("Failed to process object file {}: ", curFile, e);
			throw e;
		}
	}
	
	
	@Override
	public long readInObjects(ClientSession clientSession, Path directory, InteractingEntity importingEntity) throws IOException {
		Path objectDirPath = this.getObjDirPath(directory);
		List<File> filesForObject = getObjectFiles(objectDirPath);
		
		log.info("Found {} files for {} in {}", filesForObject.size(), this.getObjectService().getCollectionName(), objectDirPath);
		StopWatch sw = StopWatch.createStarted();
		Map<ObjectId, List<StorageBlock>> needParentMap = new HashMap<>();
		List<ObjectId> addedList = new ArrayList<>();
		for (File curObjFile : filesForObject) {
			this.readInObject(clientSession, curObjFile, importingEntity, needParentMap, addedList);
		}
		
		if(needParentMap.isEmpty()){
			log.info("No objects need parents.");
		} else {
			log.info("{} objects need parents.", needParentMap.size());
			
			while(!needParentMap.isEmpty()){
				List<ObjectId> newAddedList = new ArrayList<>(addedList.size());
				
				while (!addedList.isEmpty()){
					ObjectId curParent = addedList.remove(0);
					
					List<StorageBlock> toAdd = needParentMap.remove(curParent);
					
					if(toAdd == null){
						continue;
					}
					
					for(StorageBlock curObj : toAdd){
						this.readInObject(
							clientSession,
							curObj,
							importingEntity,
							null,
							newAddedList
						);
						
						
					}
				}
				addedList = newAddedList;
			}
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
