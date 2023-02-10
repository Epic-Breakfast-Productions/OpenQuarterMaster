package tech.ebp.oqm.baseStation.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.data.TestMainObject;
import tech.ebp.oqm.baseStation.testResources.data.TestMongoHistoriedFileService;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.lib.core.object.interactingEntity.user.User;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class MongoHistoriedFileServiceTest extends RunningServerTest {
	
	@Inject
	TestMongoHistoriedFileService testMongoService;
	
	@Inject
	TestUserService testUserService;
	
	@Test
	public void testAddFile() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		File testFile = new File(getClass().getResource("/testFiles/shakespeare.txt").getFile());
		
		
		ObjectId objectId = this.testMongoService.add(
			new TestMainObject(FAKER.lorem().paragraph()),
			testFile,
			testUser
		);
	}
	
}