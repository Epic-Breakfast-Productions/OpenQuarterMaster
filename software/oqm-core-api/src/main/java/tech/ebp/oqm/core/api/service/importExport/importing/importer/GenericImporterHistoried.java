package tech.ebp.oqm.core.api.service.importExport.importing.importer;

import com.mongodb.client.ClientSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Slf4j
public class GenericImporterHistoried<T extends MainObject, S extends SearchObject<T>>
	extends HistoriedObjectImporter<T, S, MongoHistoriedObjectService<T, S, ?>> {
	
	
	public GenericImporterHistoried(MongoHistoriedObjectService<T, S, ?> mongoService) {
		super(mongoService);
	}
	
	private void readInObject(
		ObjectId dbId,
		ClientSession clientSession,
		File curFile,
		InteractingEntity importingEntity,
		DataImportOptions options
	) throws IOException {
		try {
			T curObj = ObjectUtils.OBJECT_MAPPER.readValue(curFile, this.getObjectService().getClazz());
			
			ObjectId oldId = curObj.getId();
			ObjectId newId;
			try {
				newId = this.getObjectService().add(dbId, clientSession, curObj, importingEntity);
			} catch(Throwable e) {
				log.error("Failed to import object: ", e);
				throw e;
			}
			log.info("Read in object. new id == old? {}", newId.equals(oldId));
			assert newId.equals(oldId); //TODO:: better check?
			
		} catch(Throwable e){
			log.error("Failed to process object file {}: ", curFile, e);
			throw e;
		}
	}
	
	@Override
	protected long readInObjectsImpl(
		ObjectId dbId,
		ClientSession clientSession,
		Path objectDirPath,
		InteractingEntity importingEntity,
		DataImportOptions options,
		Map<ObjectId, ObjectId> entityIdMap
	) throws IOException {
		List<File> filesForObject = getObjectFiles(objectDirPath);
		
		log.info("Found {} files for {} in {}", filesForObject.size(), this.getObjectService().getCollectionName(), objectDirPath);
		StopWatch sw = StopWatch.createStarted();
		for (File curObjFile : filesForObject) {
			this.readInObject(dbId, clientSession, curObjFile, importingEntity, options);
		}

		if(options.getIncludeHistory()){
			//TODO:: history
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