package tech.ebp.oqm.baseStation.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.data.TestMainObject;
import tech.ebp.oqm.baseStation.testResources.data.TestMongoHistoriedService;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.lib.core.object.history.events.CreateEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.user.User;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class MongoHistoriedObjectServiceTest extends RunningServerTest {
	
	@Inject
	TestMongoHistoriedService testMongoService;
	
	@Inject
	TestUserService testUserService;
	
	@Test
	public void testEmptyHistory(){
		assertEquals(0, testMongoService.getHistoryService().count());
	}
	
	@Test
	public void testAdd(){
		User testUser = testUserService.getTestUser(true, true);
		
		
		ObjectId objectId = this.testMongoService.add(
			new TestMainObject(FAKER.lorem().paragraph()),
			testUser
		);
		
		assertEquals(1, this.testMongoService.getHistoryService().count());
		List<ObjectHistoryEvent> events = this.testMongoService.getHistoryFor(objectId);
		assertEquals(1, events.size());
		
		CreateEvent createEvent = (CreateEvent) events.get(0);
		
		
		assertEquals(objectId, createEvent.getObjectId());
		assertNotNull(createEvent.getEntity());
		assertEquals(testUser.getReference(), createEvent.getEntity());
		
	}
	
}