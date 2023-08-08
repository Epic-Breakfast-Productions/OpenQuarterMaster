package tech.ebp.oqm.baseStation.model.object.history;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.testUtils.BasicTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class ObjectHistoryEventTest extends BasicTest {
	
	public class TestObjectHistoryEvent extends DescriptiveEvent {
		
		@Override
		public EventType getType() {
			return EventType.CREATE;
		}
	}
	
	@Test
	public void testSetEntity() {
		TestObjectHistoryEvent ev = new TestObjectHistoryEvent();
		User entity = new User();
		entity.setId(new ObjectId());
		
		ev.setEntity(entity);
		
		assertEquals(entity.getId(), ev.getEntity());
	}
}
