package tech.ebp.oqm.core.api.service.importExport.importing.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.mongo.CustomUnitService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
public class UnitImporter extends TopLevelImporter<Long> {

	@Getter(AccessLevel.PRIVATE)
	@Inject
	CustomUnitService customUnitService;
	
	private static boolean isUnitNotFoundJsonException(JsonProcessingException e){
		return e.getMessage().matches("^Unit string given \\((.*)\\) does not represent any of the possible valid units.(.*)$");
	}

	@Override
	public Path getObjectDirPath(Path topLevelPath) {
		return topLevelPath.resolve(this.customUnitService.getCollectionName());
	}
	
	@Override
	public Long getNoObjDirPathExistValue() {
		return 0L;
	}
	
	private void readInUnit(
		ClientSession clientSession,
		CustomUnitEntry curObj,
		DataImportOptions options,
		List<ObjectId> addedList
	) {
		ObjectId oldId = curObj.getId();
		ObjectId newId = this.getCustomUnitService().add(clientSession, curObj);

		log.info("Read in object. new id == old? {}", newId.equals(oldId));
		assert newId.equals(oldId); //TODO:: better check?
		addedList.add(oldId);
	}

	private void readInUnit(
		ClientSession clientSession,
		File curFile,
		DataImportOptions options,
		List<File> orphanEntries,
		List<ObjectId> addedList
	) throws IOException {
		CustomUnitEntry newEntry = null;
		try {
			newEntry = ObjectUtils.OBJECT_MAPPER.readValue(curFile, CustomUnitEntry.class);
		} catch(JsonMappingException|IllegalArgumentException e){
			if(e.getMessage().contains("does not represent any of the possible valid units.")){
				orphanEntries.add(curFile);
			} else {
				log.warn("Does not look like an exception dealing with not having a parent unit.");
				throw e;
			}

			log.warn("Failed to process object file {}: ", curFile, e);
			return;
		}
		this.readInUnit(
			clientSession,
			newEntry,
			options,
			addedList
		);
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
		List<File> orphanEntries = new ArrayList<>();
		List<ObjectId> addedList = new ArrayList<>();
		StopWatch sw = StopWatch.createStarted();
		for (File curObjFile : filesForObject) {
			this.readInUnit(clientSession, curObjFile, options, orphanEntries, addedList);
		}

		if(orphanEntries.isEmpty()){
			log.info("No objects need parents.");
		} else {
			log.info("{} objects need parents.", orphanEntries.size());

			while(!orphanEntries.isEmpty()){
				while (!orphanEntries.isEmpty()){
					File entry = orphanEntries.remove(0);
					this.readInUnit(clientSession, entry, options, orphanEntries, addedList);
				}
			}
		}

		sw.stop();
		log.info(
			"Read in {} {} objects in {}",
			filesForObject.size(),
			this.getCustomUnitService().getCollectionName(),
			sw
		);
		return (long) addedList.size();
	}

}
