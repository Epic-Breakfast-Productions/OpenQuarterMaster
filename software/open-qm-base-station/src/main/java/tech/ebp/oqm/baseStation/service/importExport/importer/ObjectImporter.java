package tech.ebp.oqm.baseStation.service.importExport.importer;

import com.mongodb.client.ClientSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public abstract class ObjectImporter<T extends MainObject, S extends SearchObject<T>, M extends MongoHistoriedObjectService<T, S>> extends Importer {
	
	@Getter
	private final M objectService;
	
	protected ObjectImporter(M mongoService){
		this.objectService = mongoService;
	}
	
	protected Path getObjDirPath(Path directory){
		return directory.resolve(this.getObjectService().getCollectionName());
	}
	
	public abstract long readInObjects(
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity
	) throws IOException;
}