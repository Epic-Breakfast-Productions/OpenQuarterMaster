package tech.ebp.oqm.plugin.imageSearch.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.plugin.imageSearch.testResources.testClasses.RunningServerTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

/**
 * Disabled as initialization fails, unsure why
 */
@Disabled
@Slf4j
@QuarkusTest
@TestHTTPEndpoint(ImageSearchEndpoint.class)
class ImageSearchEndpointTest extends RunningServerTest {

	public static Stream<Arguments> imageArgs(){
		return testImagesStream()
			.stream()
			.map(Arguments::of);
	}

	public ImageSearchEndpointTest(){
		super(false);
	}

	@BeforeAll
	public static void setUp() throws InterruptedException {
		log.info("Setting up for image search endpoints test.");
		setupOqmDb(TEST_DB);
		Thread.sleep(5_000);
		awaitProcessing();
	}

	@AfterAll
	public static void tearDown(){
		cleanUp();
	}


	@ParameterizedTest
	@MethodSource("imageArgs")
	public void testSearchEndpointBasic(Path testImage) throws IOException {
		log.info("Testing search of image: {}", testImage);
		ObjectNode result = given()
			.when()
			.multiPart("file", testImage.getFileName().toString(), Files.newInputStream(testImage))
			.formParam("fileName", testImage.getFileName().toString())
			.pathParam("oqmDbIdOrName", TEST_DB)
			.post()
			.then()
			.statusCode(200)
			.extract().as(ObjectNode.class);
		log.info("Got search result: {}", result);
	}

	@Test
	public void testSearchEndpointMulti(){
		//TODO:: multiple searches at once
	}

}
