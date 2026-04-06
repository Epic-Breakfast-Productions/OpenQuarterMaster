package tech.ebp.oqm.core.api.service.importExport.importing.importer;

import com.mongodb.client.ClientSession;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class TopLevelImporter<R> extends Importer<R> {

	public abstract Path getObjectDirPath(Path topLevelPath);

	public abstract R getNoObjDirPathExistValue();
	
	public abstract R readInObjectsImpl(
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity,
		DataImportOptions options
	) throws IOException;

	public R readInObjects(
		ClientSession clientSession,
		Path topLevelPath,
		InteractingEntity importingEntity,
		DataImportOptions importOptions
	) throws IOException {
		Path objectDirPath = this.getObjectDirPath(topLevelPath);

		if(!Files.exists(objectDirPath)){
			return this.getNoObjDirPathExistValue();
		}
		return this.readInObjectsImpl(
			clientSession,
			objectDirPath,
			importingEntity,
			importOptions
		);
	}
}
