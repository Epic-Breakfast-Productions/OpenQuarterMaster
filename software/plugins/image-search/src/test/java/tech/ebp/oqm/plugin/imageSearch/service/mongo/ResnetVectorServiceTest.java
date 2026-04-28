package tech.ebp.oqm.plugin.imageSearch.service.mongo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.Filters;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.model.resnet.ImageVector;
import tech.ebp.oqm.plugin.imageSearch.testResources.testClasses.RunningServerTest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
class ResnetVectorServiceTest extends RunningServerTest {
	
	@Inject
	ResnetVectorService resnetVectorService;
	
	@Test
	public void testInitDb() {
		this.setupOqmDb(TEST_DB);
		log.info("Testing initDb");
		
		this.resnetVectorService.initVectors();
		
		log.info("Finished initDb");
		
		
		ObjectNode imageSearch = this.getOqmCoreApiClientService().imageSearch(
			this.getServiceAccountService().getAuthString(),
			TEST_DB,
			ImageSearch.builder().build()
		).await().indefinitely();
		
		assertEquals(
			imageSearch.get("numResultsForEntireQuery").asInt(),
			this.resnetVectorService.getNumVectors(TEST_DB)
		);
		
		
		for (Iterator<ImageVector> it = this.resnetVectorService.getAllVectors(TEST_DB); it.hasNext(); ) {
			ImageVector imageVector = it.next();
			
			assertNotNull(imageVector.getImageId());
			assertNotNull(imageVector.getVector());
			
			this.getOqmCoreApiClientService().imageGet(//if no exception, then it exists
				this.getServiceAccountService().getAuthString(),
				TEST_DB,
				imageVector.getImageId()
			).await().indefinitely();
		}
	}
	@Test
	public void testInitDBRemoveDeletedVector() throws IOException {
		this.setupOqmDb(TEST_DB);
		ObjectNode image;
//		try (Stream<Path> stream = Files.list(Paths.get(TEST_IMG_DIR))) {
//			List<Path> files = stream
//					.filter(Files::isRegularFile)
//					.collect(Collectors.toList());
		ObjectId id = this.resnetVectorService.getTypedCollection().insertOne(
				ImageVector.builder()
				.oqmDb(TEST_DB)
						.imageId("foo")
						.vector(new float[0])
						.imageRevision(1)
				.build()).getInsertedId().asObjectId().getValue();
		//log.debug("Added image: {}", image);
		//log.info("Testing initDb");
		this.resnetVectorService.initVectors();

		//TODO:: Check if id is present or not after initVectors allegedly deletes
		assertEquals(0, this.resnetVectorService.getTypedCollection().countDocuments(Filters.eq("_id", id)));
		//log.info("Finished initDb");
	}

	@AfterEach
	public void clearVectors(
			TestInfo testInfo
	) {
		log.info("Running after method for test {}", testInfo.getDisplayName());

		this.resnetVectorService.deleteAll();

		log.info("Completed after step.");
	}
}