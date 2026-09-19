package tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibClientHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageHelper extends CoreApiLibClientHelper {

	public static ObjectNode newImage(
		String auth,
		String oqmDbIdOrName,
		FileUploadBody upload
	) {
		return given()
			.header(new Header("Authorization", auth))
			.accept(ContentType.JSON)
			.multiPart("file", upload.fileName, upload.file)
			.formParam("fileName", upload.fileName)
			.formParam("source", "testFiles")
			.formParam("description", upload.description)
			.when()
			.pathParam("db", oqmDbIdOrName)
			.post(CoreApiLibTestUtils.getCoreApiBaseUri() + "/api/v1/{db}/media/image")
			.then()
			.statusCode(200)
			.extract().body().as(ObjectNode.class);
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
