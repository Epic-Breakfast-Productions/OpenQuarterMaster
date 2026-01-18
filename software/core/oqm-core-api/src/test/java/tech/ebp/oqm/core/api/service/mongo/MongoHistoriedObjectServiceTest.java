package tech.ebp.oqm.core.api.service.mongo;

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
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.service.notification.EventNotificationWrapper;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.testResources.data.TestMainObject;
import tech.ebp.oqm.core.api.testResources.data.TestMongoHistoriedService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.events.CreateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;

import jakarta.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
@QuarkusTestResource(value = KafkaCompanionResource.class, restrictToAnnotatedClass = true)
class MongoHistoriedObjectServiceTest extends RunningServerTest {

	@Inject
	OqmDatabaseService oqmDatabaseService;
	
	@Inject
	TestMongoHistoriedService testMongoService;
	
	@Inject
	InteractingEntityService interactingEntityService;
	
	@InjectKafkaCompanion
	KafkaCompanion kafkaCompanion;
	
	@Test
	public void testEmptyHistory(){
		assertEquals(0, testMongoService.getHistoryService().count(DEFAULT_TEST_DB_NAME));
	}
	
	@Test
	public void testAdd() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainObject newObject = this.testMongoService.add(
			DEFAULT_TEST_DB_NAME,
			new TestMainObject(FAKER.lorem().paragraph()),
			testUser
		);
		
		assertEquals(1, this.testMongoService.getHistoryService().count(DEFAULT_TEST_DB_NAME));
		List<ObjectHistoryEvent> events = this.testMongoService.getHistoryFor(DEFAULT_TEST_DB_NAME, newObject);
		assertEquals(1, events.size());
		
		CreateEvent createEvent = (CreateEvent) events.get(0);
		
		assertEquals(newObject.getId(), createEvent.getObjectId());
		assertNotNull(createEvent.getEntity());
		assertEquals(testUser.getId(), createEvent.getEntity());


		ConsumerTask<String, String> createFromAll = this.kafkaCompanion.consumeStrings().fromTopics(
			HistoryEventNotificationService.ALL_EVENT_TOPIC,
			1
		);
		createFromAll.awaitCompletion();
		assertEquals(1, createFromAll.count());
		EventNotificationWrapper createEventFromMessage = ObjectUtils.OBJECT_MAPPER.readValue(createFromAll.getFirstRecord().value(), EventNotificationWrapper.class);
		assertEquals(createEvent, createEventFromMessage.getEvent());

		// TODO: more when we want to
//		ConsumerTask<String, String> createFromAllInDb = this.kafkaCompanion.consumeStrings().fromTopics(
//			HistoryEventNotificationService.TOPIC_PREPEND + this.oqmDatabaseService.getDatabaseCache().getFromName(DEFAULT_TEST_DB_NAME).get().getDbId().toHexString() + "-" + HistoryEventNotificationService.ALL_EVENT_TOPIC_LABEL,
//			1
//		);
//		createFromAllInDb.awaitCompletion();
//		assertEquals(1, createFromAllInDb.count());
//		createEventFromMessage = ObjectUtils.OBJECT_MAPPER.readValue(createFromAllInDb.getFirstRecord().value(), EventNotificationWrapper.class);
//		assertEquals(createEvent, createEventFromMessage.getEvent());

		//TODO:: cover last type?
	}
	//TODO:: test rest
}