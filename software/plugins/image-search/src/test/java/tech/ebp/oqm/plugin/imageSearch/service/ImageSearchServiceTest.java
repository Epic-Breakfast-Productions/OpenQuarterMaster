package tech.ebp.oqm.plugin.imageSearch.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.service.mongo.ResnetVectorService;
import tech.ebp.oqm.plugin.imageSearch.testResources.testClasses.RunningServerTest;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
class ImageSearchServiceTest extends RunningServerTest {

    @Inject
    ImageSearchService imageSearchService;

    @Inject
    ResnetVectorService resnetVectorService;

    @Test
    public void testBasicSearch() throws IOException {
        this.setupOqmDb(TEST_DB);
        log.info("Testing initDb");

        this.resnetVectorService.initVectors();

        log.info("Finished initDb");

        try (Stream<Path> stream = Files.list(Paths.get(TEST_IMG_DIR)); InputStream is = Files.newInputStream(stream.findFirst().get());) {
            TreeMap<Double, String> results = this.imageSearchService.search(
                ImageSearch.builder()
                    .oqmDbIdOrName(TEST_DB)
                    .file(is)
                    .fileName("foo.png")
                    .maxResults(10)
                    .threshold(0.75)
                    .build()
            );
            log.info("Found results: {}", results);
        }
    }
}