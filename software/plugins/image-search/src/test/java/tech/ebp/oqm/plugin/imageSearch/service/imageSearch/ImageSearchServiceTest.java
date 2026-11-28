package tech.ebp.oqm.plugin.imageSearch.service.imageSearch;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.TestAuthHelper;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.model.search.SearchResults;
import tech.ebp.oqm.plugin.imageSearch.service.imageSearch.providers.resnet.ResnetProvider;
import tech.ebp.oqm.plugin.imageSearch.testResources.testClasses.RunningServerTest;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
class ImageSearchServiceTest extends RunningServerTest {

	@Inject
	ImageSearchService imageSearchService;

	@Inject
	ResnetProvider resnetProvider;

	@Getter
	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;

	@Test
	public void testBasicSearch() throws IOException {
		setupOqmDb(TEST_DB);
		log.info("Testing initVectors");

		StopWatch sw = StopWatch.createStarted();
		this.resnetProvider.initVectors();
		sw.stop();

		log.info("Finished initVectors in: {}", sw.getDuration());

		List<Path> images = testImagesStream();
		Path image = images.getFirst();
		SearchResults results = null;
		try (
			InputStream is = Files.newInputStream(image);
			) {
			results = this.imageSearchService.search(
				ImageSearch.builder()
					.oqmDbIdOrName(TEST_DB)
					.file(is)
					.fileName("foo.png")
					.maxResults(10)
					.threshold(0.75)
					.build()
			);
		}
		log.info("Found results: {}", results);

		ObjectNode item = this.getOqmCoreApiClientService()
			.invItemGet(TestAuthHelper.getAuthString(), TEST_DB, results.getResults().first().getItem()).await().indefinitely();
		log.info("Item matched most: {}", item);

		ObjectNode imageFromItem = this.getOqmCoreApiClientService()
			.imageGet(TestAuthHelper.getAuthString(), TEST_DB, item.get("imageIds").get(0).asText()).await().indefinitely();
		log.info("Image from matched item: {}", imageFromItem);

		assertEquals(
			image.getFileName().toString(),
			imageFromItem.get("fileName").asText()
		);
	}
}
