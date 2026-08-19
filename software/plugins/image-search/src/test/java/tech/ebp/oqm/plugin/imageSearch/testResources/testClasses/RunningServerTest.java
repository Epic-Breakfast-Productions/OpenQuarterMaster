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
        log.info("Before test " + testInfo.getTestMethod().get().getName());
    }

    @AfterEach
    public void afterEach(
            TestInfo testInfo
    ) {
        log.info("Running after method for test {}", testInfo.getDisplayName());

        this.oqmCoreApiClientService.manageDbClearAll(this.serviceAccountService.getAuthString()).await().indefinitely();

        log.info("Completed after step.");
    }


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

                ObjectNode image;

                try (
                        InputStream is = Files.newInputStream(path);
                ) {
                    image = this.oqmCoreApiClientService.imageAdd(
                            this.serviceAccountService.getAuthString(),
                            dbName,
                            FileUploadBody.builder()
                                    .fileName(fileName)
                                    .file(is)
                                    .description("Test Image")
                                    .source("testFiles")
                                    .build()
                    ).await().indefinitely();
                }
                log.debug("Added image: {}", image);


                ObjectNode curItem = objectMapper.createObjectNode()
                        .put("name", itemName)
                        .put("storageType", "BULK");
                curItem.putArray("imageIds").add(image.get("id").asText());
                curItem.putObject("unit").put("string", "units");

                curItem = this.oqmCoreApiClientService.invItemCreate(
                        this.serviceAccountService.getAuthString(),
                        dbName,
                        curItem
                ).await().indefinitely();

                log.debug("Added item: {}", curItem);

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Completed setting up OQM Core API database with test images.");
    }

    public ObjectNode testGetTestImage() throws IOException {
        try (
                Stream<Path> stream = Files.list(Paths.get(TEST_IMG_DIR));
                InputStream is = Files.newInputStream(stream.findFirst().get());
        ) {
            return this.getOqmCoreApiClientService().imageAdd(
                    this.getServiceAccountService().getAuthString(),
                    TEST_DB,
                    FileUploadBody.builder()
                            .fileName("testFoo.jpg")
                            .file(is)
                            .description("Test Image")
                            .source("testFiles")
                            .build()
            ).await().indefinitely();
        }
    }

}
