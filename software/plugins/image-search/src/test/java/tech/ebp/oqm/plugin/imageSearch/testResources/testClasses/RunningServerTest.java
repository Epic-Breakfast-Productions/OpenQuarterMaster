package tech.ebp.oqm.plugin.imageSearch.testResources.testClasses;

import com.fasterxml.jackson.databind.JsonNode;
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
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibTestDbManager;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers.ImageHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers.ItemHelper;
import tech.ebp.oqm.plugin.imageSearch.testResources.testUsers.TestUserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;


@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
public abstract class RunningServerTest extends WebServerTest {

	public static final String TEST_DB = "default"; //TODO:: instead of using this, get actual id from db
	public static final String TEST_IMG_DIR = "./dev/testImages/";

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

		CoreApiLibTestDbManager.clearAllDbs(this.serviceAccountService.getAuthString());

		log.info("Completed after step.");
	}


	/**
	 * TODO::: figure out how to integration test. Auth is the biggest issue
	 * @param dbName
	 */
	protected void setupOqmDb(String dbName) {
		//TODO:: setup core api database with images, items, etc
		log.info("Setting up OQM Core API database with test images.");
		try (Stream<Path> stream = Files.list(Paths.get(TEST_IMG_DIR))) {
			List<Path> files = stream
								   .filter(Files::isRegularFile)
								   .collect(Collectors.toList());

			for (Path path : files) {
				log.info("Adding Item/ file: {}", path.getFileName());
				String fileName = path.getFileName().toString();
				String itemName = fileName.toLowerCase()
									  .substring(0, fileName.lastIndexOf('.'))
									  .replaceAll("_", " ")
									  .strip();

				ObjectNode image = ImageHelper.newImage(
					this.serviceAccountService.getAuthString(),
					dbName,
					fileName,
					path
				);

				log.debug("Added image: {}", image);

				ObjectNode curItem = ItemHelper.getItemTemplate("BULK");
				curItem.putArray("imageIds").add(image.get("id").asText());

				curItem = ItemHelper.addItem(
					this.serviceAccountService.getAuthString(),
					dbName,
					curItem
				);

				log.debug("Added item: {}", curItem);

			}

		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		log.info("Completed setting up OQM Core API database with test images.");
	}
}
