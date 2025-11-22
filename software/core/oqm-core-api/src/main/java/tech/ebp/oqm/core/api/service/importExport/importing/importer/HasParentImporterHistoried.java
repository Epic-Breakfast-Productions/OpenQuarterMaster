package tech.ebp.oqm.core.api.service.importExport.importing.importer;

import com.mongodb.client.ClientSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.HasParent;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.core.api.exception.db.DbModValidationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO:: changeover to generic 'parentImporter' to operate on the HasParent interface
 */
@Slf4j
public class HasParentImporterHistoried<T extends MainObject & HasParent, S extends SearchObject<T>>
	extends ObjectImporter<T, S, MongoHistoriedObjectService<T, S, ?>> {
	
	public HasParentImporterHistoried(MongoHistoriedObjectService<T, S, ?> mongoService) {
		super(mongoService);
	}
	
	private void readInObject(
		ObjectId dbId,
		ClientSession clientSession,
		T curObj,
		InteractingEntity importingEntity,
		DataImportOptions options,
		Map<ObjectId, ObjectId> entityIdMap,
		Map<ObjectId, List<T>> needParentMap,
		List<ObjectId> addedList
	) {
		ObjectId oldId = curObj.getId();
		ObjectId newId;
		try {
			newId = this.getObjectService().add(dbId, clientSession, curObj, importingEntity);
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
		ObjectId dbId,
		ClientSession clientSession,
		File curFile,
		InteractingEntity importingEntity,
		DataImportOptions options,
		Map<ObjectId, ObjectId> entityIdMap,
		Map<ObjectId, List<T>> needParentMap,
		List<ObjectId> addedList
	) throws IOException {
		try {
			this.readInObject(
				dbId,
				clientSession,
				ObjectUtils.OBJECT_MAPPER.readValue(curFile, this.getObjectService().getClazz()),
				importingEntity,
				options,
				entityIdMap,
				needParentMap,
				addedList
			);
		} catch(Throwable e){
			log.error("Failed to process object file {}: ", curFile, e);
			throw e;
		}
	}
	
	
	@Override
	protected long readInObjectsImpl(
		ObjectId dbId,
		ClientSession clientSession, Path objectDirPath, InteractingEntity importingEntity,
		DataImportOptions options,
		Map<ObjectId, ObjectId> entityIdMap
	) throws IOException {
		List<File> filesForObject = getObjectFiles(objectDirPath);
		
		log.info("Found {} files for {} in {}", filesForObject.size(), this.getObjectService().getCollectionName(), objectDirPath);
		StopWatch sw = StopWatch.createStarted();
		Map<ObjectId, List<T>> needParentMap = new HashMap<>();
		List<ObjectId> addedList = new ArrayList<>();
		for (File curObjFile : filesForObject) {
			this.readInObject(dbId, clientSession, curObjFile, importingEntity, options, entityIdMap, needParentMap, addedList);
		}
		
		if(needParentMap.isEmpty()){
			log.info("No objects need parents.");
		} else {
			log.info("{} objects need parents.", needParentMap.size());
			
			while(!needParentMap.isEmpty()){
				List<ObjectId> newAddedList = new ArrayList<>(addedList.size());
				
				while (!addedList.isEmpty()){
					ObjectId curParent = addedList.remove(0);
					
					List<T> toAdd = needParentMap.remove(curParent);
					
					if(toAdd == null){
						continue;
					}
					
					for(T curObj : toAdd){
						this.readInObject(
							dbId,
							clientSession,
							curObj,
							importingEntity,
							options,
							entityIdMap,
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
