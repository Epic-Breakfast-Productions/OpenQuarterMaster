package tech.ebp.oqm.baseStation.service.importExport.importer;

import com.mongodb.client.ClientSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.file.MongoHistoriedFileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public abstract class FileImporter<T extends FileMainObject, S extends SearchObject<T>, M extends MongoHistoriedFileService<T, S>> extends Importer {
	
	@Getter
	private final M fileService;
	
	@Getter
	private final ObjectImporter<T, S, ?> objectImporter;
	
	protected FileImporter(M fileService){
		this.fileService = fileService;
		this.objectImporter = new GenericImporter<T, S>(fileService.getFileObjectService());
	}
	
	protected Path getFileObjDirPath(Path directory){
		return directory.resolve(this.getFileService().getCollectionName());
	}
	
	public abstract long readInObjects(
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity
	) throws IOException;
}