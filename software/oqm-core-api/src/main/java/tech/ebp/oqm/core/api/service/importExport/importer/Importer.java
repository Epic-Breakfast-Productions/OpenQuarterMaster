package tech.ebp.oqm.core.api.service.importExport.importer;

import com.mongodb.client.ClientSession;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Importer {
	
	/**
	 * Gets all files in a directory that end in "<code>.json</code>"
	 * @param directory
	 * @return
	 * @throws IOException
	 */
	protected static List<File> getObjectFiles(Path directory) throws IOException {
		try (
			Stream<Path> paths = Files.walk(
				directory,
				1
			)
		) {
			return paths
					   .filter(Files::isRegularFile)
					   .filter((Path path)->{
						   return path.toString().endsWith(".json");
					   })
					   .map(Path::toFile)
					   .collect(Collectors.toList());
		}
	}
	
	public abstract long readInObjects(
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity
	) throws IOException;
}
