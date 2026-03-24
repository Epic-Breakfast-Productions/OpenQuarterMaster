package tech.ebp.oqm.plugin.imageSearch.service.mongo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.model.resnet.ImageVector;
import tech.ebp.oqm.plugin.imageSearch.testResources.testClasses.RunningServerTest;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
class ResnetVectorServiceTest extends RunningServerTest {
	
	@Inject
	ResnetVectorService resnetVectorService;
	
	@Test
	public void testInitDb() {
		this.setupDb(TEST_DB);
		log.info("Testing initDb");
		
		this.resnetVectorService.initVectors();
		
		log.info("Finished initDb");
		
		
		ObjectNode imageSearch = this.getOqmCoreApiClientService().imageSearch(
			this.getServiceAccountService().getAuthString(),
			TEST_DB,
			new ImageSearch() //TODO:: swap to builder after lib updates
		).await().indefinitely();
		
		assertEquals(
			imageSearch.get("numResultsForEntireQuery").asInt(),
			this.resnetVectorService.getNumVectors(TEST_DB)
		);
		
		
		for (Iterator<ImageVector> it = this.resnetVectorService.getAllVectors(TEST_DB); it.hasNext(); ) {
			ImageVector imageVector = it.next();
			
			assertNotNull(imageVector.getId());
			assertNotNull(imageVector.getVector());
			
			this.getOqmCoreApiClientService().imageGet(//if no exception, then it exists
				this.getServiceAccountService().getAuthString(),
				TEST_DB,
				imageVector.getImageId()
			).await().indefinitely();
		}
	}
	
}