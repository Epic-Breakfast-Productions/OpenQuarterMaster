package tech.ebp.oqm.plugin.imageSearch.testResources.testClasses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibTestDbManager;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.TestAuthHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers.ImageHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers.ItemHelper;
import tech.ebp.oqm.plugin.imageSearch.testResources.testUsers.TestUserService;

import java.io.IOException;
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

	@Getter
	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;

	@Getter
	@Inject
	KcClientAuthService serviceAccountService;

	@Getter
	@ConfigProperty(name = "oqm.core.api.baseUri")
	String coreApiBaseUri;


	@Getter
	private final TestUserService testUserService = TestUserService.getInstance();

	@Inject
	ObjectMapper objectMapper;

	@BeforeEach
	public void beforeEach(TestInfo testInfo) {
		log.info("Before test {}", testInfo.getTestMethod().get().getName());
	}

	@AfterEach
	public void afterEach(
		TestInfo testInfo
	) {
		log.info("Running after method for test {}", testInfo.getDisplayName());

		CoreApiLibTestDbManager.clearAllDbs(TestAuthHelper.getAuthString());

		log.info("Completed after step.");
	}


	/**
	 * TODO::: figure out how to integration test. Auth is the biggest issue
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
		return given()
			.when()
			.get("/api/v1/status")
			.then()
			.statusCode(200)
			.extract().as(ObjectNode.class)
			.get("processing").asBoolean();
	}

	protected static void awaitProcessing(Duration timeout) {
		Instant start = Instant.now();
		log.info("Waiting for processing of images to complete...");
		do{
			if(!isProcessing()){
				log.info("Done processing.");
				return;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} while (Duration.between(start, Instant.now()).compareTo(timeout) < 0);

		fail("Failed to wait for processing to complete.");
	}
}
