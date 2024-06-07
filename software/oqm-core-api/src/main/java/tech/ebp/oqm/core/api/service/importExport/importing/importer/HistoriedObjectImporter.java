package tech.ebp.oqm.core.api.service.importExport.importing.importer;

import com.mongodb.client.ClientSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
public abstract class HistoriedObjectImporter<T extends MainObject, S extends SearchObject<T>, M extends MongoHistoriedObjectService<T, S, ?>>
	extends Importer<T> {
	
	@Getter
	private final M objectService;
	
	protected HistoriedObjectImporter(M mongoService){
		this.objectService = mongoService;
	}
	
	protected Path getObjDirPath(Path directory){
		return directory.resolve(this.getObjectService().getCollectionName());
	}
	
	protected abstract long readInObjectsImpl(
		ObjectId dbId,
		ClientSession clientSession,
		Path objectDirPath,
		InteractingEntity importingEntity,
		DataImportOptions importOptions,
		Map<ObjectId, ObjectId> entityIdMap
	) throws IOException;

	public long readInObjects(
		ObjectId dbId,
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity,
		DataImportOptions importOptions,
		Map<ObjectId, ObjectId> entityIdMap
	) throws IOException{
		Path objectDirPath = this.getObjDirPath(directory);
		
		if(!Files.exists(objectDirPath)){
			return 0;
		}
		return this.readInObjectsImpl(
			dbId,
			clientSession,
			objectDirPath,
			importingEntity,
			importOptions,
			entityIdMap
		);
	}
	
}