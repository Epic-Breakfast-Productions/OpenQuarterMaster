package tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibClientHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageHelper extends CoreApiLibClientHelper {

	public static ObjectNode newImage(
		String auth,
		String oqmDbIdOrName,
		FileUploadBody upload
	) {
		return getCoreApiClientService()
				   .imageAdd(
					   auth,
					   oqmDbIdOrName,
					   upload
				   )
				   .await().indefinitely();
	}

	public static ObjectNode newImage(
		String auth,
		String oqmDbIdOrName,
		String imageName,
		Path imageFile
	) throws IOException {
		try (
			InputStream is = Files.newInputStream(imageFile);
		) {
			return newImage(
				auth,
				oqmDbIdOrName,
				FileUploadBody.builder()
					.fileName(imageName)
					.file(is)
					.description("Test Image")
					.source("testFiles")
					.build()
			);
		}
	}

	public static ObjectNode newImage(
		String auth,
		String oqmDbIdOrName,
		Path imageFile
	) throws IOException {
		return newImage(
			auth,
			oqmDbIdOrName,
			imageFile.getFileName().toString(),
			imageFile
		);
	}
}
