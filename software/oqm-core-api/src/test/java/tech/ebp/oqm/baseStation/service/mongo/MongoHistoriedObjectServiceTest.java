package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;
import tech.ebp.oqm.baseStation.service.notification.HistoryEventNotificationService;
import tech.ebp.oqm.baseStation.testResources.data.TestMainObject;
import tech.ebp.oqm.baseStation.testResources.data.TestMongoHistoriedService;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.CreateEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;

import jakarta.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
@QuarkusTestResource(value = KafkaCompanionResource.class, restrictToAnnotatedClass = true)
class MongoHistoriedObjectServiceTest extends RunningServerTest {
	
	@Inject
	TestMongoHistoriedService testMongoService;
	
	@Inject
	TestUserService testUserService;
	
	@Inject
	InteractingEntityService interactingEntityService;
	
	@InjectKafkaCompanion
	KafkaCompanion kafkaCompanion;
	
	@Test
	public void testEmptyHistory(){
		assertEquals(0, testMongoService.getHistoryService().count());
	}
	
	@Test
	public void testAdd() throws JsonProcessingException {
		User testUser = testUserService.getTestUser();
		this.interactingEntityService.add(testUser);
		
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
		assertEquals(testUser.getId(), createEvent.getEntity());
		
		ConsumerTask<String, String> createFromAll = this.kafkaCompanion.consumeStrings().fromTopics(HistoryEventNotificationService.ALL_EVENT_TOPIC, 1);
		createFromAll.awaitCompletion();
		assertEquals(1, createFromAll.count());
		CreateEvent createEventFromMessage = ObjectUtils.OBJECT_MAPPER.readValue(createFromAll.getFirstRecord().value(), CreateEvent.class);
		assertEquals(createEvent, createEventFromMessage);
		
		ConsumerTask<String, String> createFromCreate = this.kafkaCompanion.consumeStrings().fromTopics(HistoryEventNotificationService.ALL_EVENT_TOPIC, 1);
		createFromCreate.awaitCompletion();
		assertEquals(1, createFromCreate.count());
		createEventFromMessage = ObjectUtils.OBJECT_MAPPER.readValue(createFromCreate.getFirstRecord().value(), CreateEvent.class);
		assertEquals(createEvent, createEventFromMessage);
	}
	//TODO:: test rest
}