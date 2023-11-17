package tech.ebp.oqm.baseStation.service.importExport.importer;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.media.file.FileAttachment;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.baseStation.service.mongo.file.MongoHistoriedFileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class GenericFileImporter<T extends FileMainObject, S extends SearchObject<T>> extends FileImporter<T, S, MongoHistoriedFileService<T, S>> {
	
	
	public GenericFileImporter(MongoHistoriedFileService<T, S> fileService) {
		super(fileService);
	}
	
	@Override
	public long readInObjects(ClientSession clientSession, Path directory, InteractingEntity importingEntity) throws IOException {
		Path mainDir = this.getFileObjDirPath(directory);
		Path filesFir = mainDir.resolve("files");
		
		this.getObjectImporter().readInObjects(clientSession, mainDir, importingEntity);
		long result = 0;
		
		try (
			MongoCursor<T> it = this.getFileService().getFileObjectService().listIterator(clientSession).iterator();
		) {
			while (it.hasNext()) {
				T curFileObj = it.next();
				String curFileName = curFileObj.getFileName();
				
				//TODO:: actually read in files
				
				
				
				
			}
		}
		
		return result;
	}
}