package tech.ebp.oqm.plugin.imageSearch.testResources.testClasses;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibTestDbManager;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.TestAuthHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers.ImageHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers.ItemHelper;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;


@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
@NoArgsConstructor
public abstract class RunningServerTest extends WebServerTest {

	public static final String TEST_DB = "default"; //TODO:: instead of using this, get actual id from db
	public static final String TEST_IMG_DIR = "./dev/testImages/";

	protected static List<Path> testImagesStream() {
		try (Stream<Path> stream = Files.list(Paths.get(TEST_IMG_DIR))) {
			return stream
				.filter(Files::isRegularFile)
				.filter(path -> {
					return path.toString().endsWith(".jpg");
				})
				.toList();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	boolean clearCoreApiAfterEach = true;

	protected RunningServerTest(boolean clearCoreApiAfterEach) {
		this.clearCoreApiAfterEach = clearCoreApiAfterEach;
	}

	@BeforeEach
	public void beforeEach(TestInfo testInfo) {
		log.info("Before test {}", testInfo.getTestMethod().get().getName());
	}

	@AfterEach
	public void afterEach(
		TestInfo testInfo
	) {
		log.info("Running after method for test {}", testInfo.getDisplayName());

		if (this.clearCoreApiAfterEach) {
			this.cleanUp();
		}

		log.info("Completed after step.");
	}

	protected static void cleanUp() {
		CoreApiLibTestDbManager.clearAllDbs(TestAuthHelper.getAuthString());
	}


	/**
	 *
	 * @param dbName
	 */
	protected static void setupOqmDb(String dbName) {
		log.info("Setting up OQM Core API database with test images.");
		List<Path> files = testImagesStream();

		for (Path path : files) {
			log.info("Adding Item/ file: {}", path.getFileName());
			String fileName = path.getFileName().toString();
			String itemName = fileName.toLowerCase()
				.substring(0, fileName.lastIndexOf('.'))
				.replaceAll("_", " ")
				.strip();

			ObjectNode image = null;
			try {
				image = ImageHelper.newImage(
					TestAuthHelper.getAuthString(),
					dbName,
					fileName,
					path
				);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			log.debug("Added image: {}", image);

			ObjectNode curItem = ItemHelper.getItemTemplate("BULK");
			curItem.put("name", itemName);
			curItem.putArray("imageIds").add(image.get("id").asText());

			curItem = ItemHelper.addItem(
				TestAuthHelper.getAuthString(),
				dbName,
				curItem
			);

			log.debug("Added item: {}", curItem);

		}

		log.info("Completed setting up OQM Core API database with test images.");
	}

	protected static boolean isProcessing() {
		try {
			return given()
				.basePath("")
				.when()
				.get(URI.create("http://localhost:8081/api/v1/status"))
				.then()
				.statusCode(200)
				.extract().as(ObjectNode.class)
				.get("processing").asBoolean();
		} catch (Exception | AssertionError e) {
			log.warn("FAILED calling for status: ", e);
			return true;//just returning true to continue await loop
		}
	}

	protected static void awaitProcessing(Duration timeout) {
		Instant start = Instant.now();
		log.info("Waiting for processing of images to complete...");
		do {
			if (!isProcessing()) {
				log.info("Done processing.");
				return;
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} while (Duration.between(start, Instant.now()).compareTo(timeout) < 0);

		fail("Failed to wait for processing to complete.");
	}

	protected static void awaitProcessing() {
		awaitProcessing(Duration.ofMinutes(5));
	}
}