package tech.ebp.oqm.core.api.service.importExport.importer;

import com.mongodb.client.ClientSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public abstract class ObjectImporter<T extends MainObject, S extends SearchObject<T>, M extends MongoHistoriedObjectService<T, S, ?>> extends Importer {
	
	@Getter
	private final M objectService;
	
	protected ObjectImporter(M mongoService){
		this.objectService = mongoService;
	}
	
	protected Path getObjDirPath(Path directory){
		return directory.resolve(this.getObjectService().getCollectionName());
	}
	
	protected abstract long readInObjectsImpl(
		ClientSession clientSession,
		Path objectDirPath,
		InteractingEntity importingEntity
	) throws IOException;
	
	@Override
	public long readInObjects(
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity
	) throws IOException{
		Path objectDirPath = this.getObjDirPath(directory);
		
		if(!Files.exists(objectDirPath)){
			return 0;
		}
		return this.readInObjectsImpl(
			clientSession,
			objectDirPath,
			importingEntity
		);
	}
	
}