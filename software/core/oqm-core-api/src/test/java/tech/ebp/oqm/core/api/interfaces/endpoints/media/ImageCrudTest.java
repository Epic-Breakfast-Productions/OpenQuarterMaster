package tech.ebp.oqm.core.api.interfaces.endpoints.media;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.rest.media.ImageGet;
import tech.ebp.oqm.core.api.service.mongo.ImageServiceTest;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

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
	public void addImageTest(Path testImage) throws JsonProcessingException {
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
	}

	//TODO:: search, empty

	//TODO::  search, results

	//TODO:: update object

	//TODO:: update image file

	//TODO:: get revision object

	//TODO:: get revision data

	//TODO:: history
}
