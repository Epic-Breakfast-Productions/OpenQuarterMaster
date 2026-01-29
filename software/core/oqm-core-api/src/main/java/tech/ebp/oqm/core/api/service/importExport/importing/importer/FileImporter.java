package tech.ebp.oqm.core.api.service.importExport.importing.importer;

import com.mongodb.client.ClientSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.core.api.model.object.FileMainObject;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.model.rest.search.FileSearchObject;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.mongo.file.MongoHistoriedFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
public abstract class FileImporter<
	T extends FileMainObject,
	U extends FileUploadBody,
	S extends FileSearchObject<T>,
	G extends MainObject & FileGet,
	M extends MongoHistoriedFileService<T, U, S, G>
	>
	extends Importer<T> {

	@Getter
	private final M fileService;

	@Getter
	private final ObjectImporter<T, S, ?> historiedObjectImporter;

	protected FileImporter(M fileService) {
		this.fileService = fileService;
		this.historiedObjectImporter = new GenericImporterHistoried<T, S>(fileService.getFileObjectService());
	}

	protected Path getFileObjDirPath(Path directory) {
		return directory.resolve(this.getFileService().getCollectionName());
	}

	protected abstract long readInObjectsImpl(
		ObjectId dbId,
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity,
		DataImportOptions options,
		Map<ObjectId, ObjectId> entityIdMap
	) throws IOException;

	public long readInObjects(
		ObjectId dbId,
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity,
		DataImportOptions options,
		Map<ObjectId, ObjectId> entityIdMap
	) throws IOException {
		Path objectDirPath = this.getFileObjDirPath(directory);

		if (!Files.exists(objectDirPath)) {
			return 0;
		}
		return this.readInObjectsImpl(
			dbId,
			clientSession,
			objectDirPath,
			importingEntity,
			options,
			entityIdMap
		);
	}

}