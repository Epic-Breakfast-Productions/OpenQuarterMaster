package tech.ebp.oqm.core.api.interfaces.endpoints.media;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.rest.media.ImageGet;
import tech.ebp.oqm.core.api.service.mongo.ImageServiceTest;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Tag("integration")
@Slf4j
@QuarkusTest
@TestHTTPEndpoint(ImageCrud.class)
class ImageCrudTest extends RunningServerTest {

	public static Stream<Arguments> getTestImageArgs() {
		return ImageServiceTest.getTestImageStream()
				   .map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("getTestImageArgs")
	public void addImageTest(Path testImage) throws IOException {
		log.info("Testing adding file: {}", testImage);
		User testUser = this.getTestUserService().getTestUser();

		String resultStr =
			setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
				.accept(ContentType.JSON)
				.multiPart("file", testImage.getFileName().toString(), ImageCrudTest.class.getResourceAsStream(testImage.toString()))
				.formParam("fileName", testImage.getFileName().toString())
				.formParam("source", "testFiles")
				.formParam("description", FAKER.lorem().paragraph())
				.when()
				.pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
				.post()
				.then()
				.statusCode(200)
				.extract().body().asString();

		log.info("Image get String returned: {}", resultStr);

		ImageGet result = ObjectUtils.OBJECT_MAPPER.readValue(resultStr, ImageGet.class);

		log.info("Image get returned: {}", result);


		FileMetadata metadata = result.getRevisions().get(result.getLatestRevision() - 1);

		log.info("Image metadata: {}", metadata);

		ValidatableResponse imageDataResult = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
												  .accept(ContentType.JSON)
												  .when()
												  .pathParam("oqmDbIdOrName", DEFAULT_TEST_DB_NAME)
												  .pathParam("imageId", result.getId().toHexString())
												  .pathParam("revision", result.getLatestRevision())
												  .get("/{imageId}/revision/{revision}/data")
												  .then()
												  .statusCode(200)
												  .contentType(metadata.getMimeType());


		byte[] imageData = IOUtils.toByteArray(
								imageDataResult
							   .extract().body().asInputStream()
		);

		byte[] origData = IOUtils.resourceToByteArray(testImage.toString());

		if (metadata.getMimeType().equals("image/svg+xml")) {
			assertArrayEquals(
				origData,
				imageData
			);
		} else {
			ImageServiceTest.assertImageSame(
				ImageIO.read(new ByteArrayInputStream(origData)),
				ImageIO.read(new ByteArrayInputStream(imageData))
			);
		}
	}

	//TODO:: search, empty

	//TODO::  search, results

	//TODO:: update object

	//TODO:: update image file

	//TODO:: get revision object

	//TODO:: get revision data

	//TODO:: history
}
