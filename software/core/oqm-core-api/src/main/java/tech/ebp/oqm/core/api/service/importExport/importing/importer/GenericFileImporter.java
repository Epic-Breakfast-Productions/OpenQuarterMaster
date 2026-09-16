package tech.ebp.oqm.core.api.service.importExport.importing.importer;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.core.api.model.object.FileMainObject;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;
import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.model.rest.search.FileSearchObject;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.mongo.file.MongoHistoriedFileService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class GenericFileImporter<
	T extends FileMainObject,
	U extends FileUploadBody,
	S extends FileSearchObject<T>,
	G extends MainObject & FileGet
	>
	extends FileImporter<T, U, S, G, MongoHistoriedFileService<T, U, S, G>> {
	
	
	public GenericFileImporter(MongoHistoriedFileService<T, U, S, G> fileService) {
		super(fileService);
	}
	
	@Override
	protected long readInObjectsImpl(
		ObjectId dbId,
		ClientSession clientSession,
		Path mainDir,
		InteractingEntity importingEntity,
		DataImportOptions options,
		Map<ObjectId, ObjectId> entityIdMap
	) throws IOException {
		Path filesDir = mainDir.resolve("files");
		
		this.getHistoriedObjectImporter().readInObjectsImpl(dbId, clientSession, mainDir, importingEntity, options, entityIdMap);
		long result = 0;
		
		try (
			MongoCursor<T> it = this.getFileService().getFileObjectService().listIterator(dbId, clientSession).iterator();
		) {
			while (it.hasNext()) {
				T curFileObj = it.next();
				String curFileName = curFileObj.getGridfsFileName();
				Path curFileDir = filesDir.resolve(curFileName);
				
				List<Path> fileVersionDirs;
				try (
					Stream<Path> paths = Files.walk(
						curFileDir,
						1
					)
				) {
					fileVersionDirs = paths
							   .filter(Files::isDirectory)
										  .filter((Path cur) -> !curFileDir.equals(cur))
							   .collect(Collectors.toList());
				}
				
				for(Path curFileVersionDir : fileVersionDirs) {
					FileMetadata metadataIn = ObjectUtils.OBJECT_MAPPER.readValue(curFileVersionDir.resolve("metadata.json").toFile(), FileMetadata.class);
					Path actualFile = curFileVersionDir.resolve("file." + metadataIn.getFileExtension());
					FileMetadata metadataRead = new FileMetadata(actualFile.toFile());
					
					if(!metadataRead.getHashes().equals(metadataIn.getHashes())){
						log.error("Failed to verify hashes of file being imported. Metadata read: {}, Metadata from import: {}", metadataRead, metadataIn);
						throw new RuntimeException("File being read in had invalid hash(es).");
					}
					
					GridFSBucket bucket = this.getFileService().getGridFSBucket(dbId);
					
					GridFSUploadOptions ops = this.getFileService().getUploadOps(metadataIn);
					String filename = curFileObj.getId().toHexString() + "." + metadataIn.getFileExtension();
					
					try(
						InputStream is = Files.newInputStream(actualFile)
						) {
						bucket.uploadFromStream(clientSession, filename, is, ops);
					}
				}
			}
		}
		
		return result;
	}
}