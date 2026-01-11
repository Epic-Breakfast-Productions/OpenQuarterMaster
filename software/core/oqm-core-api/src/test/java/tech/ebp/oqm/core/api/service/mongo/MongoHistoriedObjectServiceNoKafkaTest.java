package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.events.CreateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.service.notification.EventNotificationWrapper;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.testResources.data.TestMainObject;
import tech.ebp.oqm.core.api.testResources.data.TestMongoHistoriedService;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.profiles.NoKafkaTest;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
@TestProfile(NoKafkaTest.class)
class MongoHistoriedObjectServiceNoKafkaTest extends RunningServerTest {
	
	@Inject
	TestMongoHistoriedService testMongoService;
	
	@Test
	public void testEmptyHistory() {
		assertEquals(0, testMongoService.getHistoryService().count(DEFAULT_TEST_DB_NAME));
	}
	
	@Test
	public void testAdd() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();
		
		ObjectId objectId = this.testMongoService.add(DEFAULT_TEST_DB_NAME, new TestMainObject(FAKER.lorem().paragraph()), testUser).getId();
		
		assertEquals(1, this.testMongoService.getHistoryService().count(DEFAULT_TEST_DB_NAME));
		List<ObjectHistoryEvent> events = this.testMongoService.getHistoryFor(DEFAULT_TEST_DB_NAME, objectId);
		assertEquals(1, events.size());
		
		CreateEvent createEvent = (CreateEvent) events.get(0);
		
		assertEquals(objectId, createEvent.getObjectId());
		assertNotNull(createEvent.getEntity());
		assertEquals(testUser.getId(), createEvent.getEntity());
	}
}