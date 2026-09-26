package tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibClientHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageHelper extends CoreApiLibClientHelper {

	private static final AtomicInteger count = new AtomicInteger();

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
				   .post(CoreApiLibTestUtils.getCoreApiBaseUri() + "/api/v1/db/{db}/media/image")
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

	public static ObjectNode newImage(
		String auth,
		String oqmDbIdOrName
	) throws IOException {

		ByteArrayInputStream is;
		{
			Random r = new Random();
			StringBuilder sb = new StringBuilder()
								   .append("<svg xmlns='http://www.w3.org/2000/svg' width='400' height='400'>");

			for (int i = 0; i < 5; i++) {
				int c = 1 + r.nextInt(3); // shape kind
				switch (c) {
					case 1 -> sb.append(String.format(
						"<circle cx='%d' cy='%d' r='%d' fill='%06x'/>",
						r.nextInt(400), r.nextInt(400), 10 + r.nextInt(30), r.nextInt(0xffffff)
					));
					case 2 -> sb.append(String.format(
						"<rect x='%d' y='%d' width='%d' height='%d' fill='%06x'/>",
						r.nextInt(380), r.nextInt(380), 20 + r.nextInt(40), 20 + r.nextInt(40), r.nextInt(0xffffff)
					));
					case 3 -> sb.append(String.format(
						"<polygon points='%d,%d %d,%d %d,%d' fill='%06x'/>",
						(int) (200 + 60 * Math.cos(r.nextInt(6283) / 1000.0)), (int) (200 + 60 * Math.sin(r.nextInt(6283) / 1000.0)),
						(int) (200 + 60 * Math.cos(r.nextInt(6283) / 1000.0)), (int) (200 + 60 * Math.sin(r.nextInt(6283) / 1000.0)),
						(int) (200 + 60 * Math.cos(r.nextInt(6283) / 1000.0)), (int) (200 + 60 * Math.sin(r.nextInt(6283) / 1000.0)),
						r.nextInt(0xffffff)
					));
				}
			}
			sb.append("</svg>");

			String svgData = sb.toString();
			log.debug("SVG Data: {}", svgData);

			is = new ByteArrayInputStream(svgData.getBytes());
		}

		return newImage(
			auth,
			oqmDbIdOrName,
			FileUploadBody.builder()
				.file(is)
				.fileName(count.incrementAndGet() + "-generatedTestImage.svg")
				.description(getFaker().lorem().paragraph())
				.source("generated test image")
				.build()
		);
	}
}
