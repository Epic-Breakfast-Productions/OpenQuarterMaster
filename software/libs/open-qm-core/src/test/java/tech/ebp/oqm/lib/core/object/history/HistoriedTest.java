package tech.ebp.oqm.lib.core.object.history;

import tech.ebp.oqm.lib.core.object.history.events.CreateEvent;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;
import tech.ebp.oqm.lib.core.object.history.events.UpdateEvent;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class HistoriedTest extends BasicTest {
	
	public ObjectHistoryEvent getBasicTestItem() {
		return new ObjectHistoryEvent();
	}
	
	@Test
	public void testUpdated() {
		ObjectHistoryEvent o = this.getBasicTestItem();
		
		assertTrue(o.getHistory().isEmpty());
		assertEquals(0, o.getHistory().size());
		
		ObjectHistoryEvent o2 = o.updated(
			CreateEvent.builder().build()
		);
		assertSame(o, o2);
		assertEquals(1, o.getHistory().size());
		
		o2 = o.updated(
			UpdateEvent.builder().build()
		);
		
		assertTrue(o.getHistory().get(0).getTimestamp().isAfter(o.getHistory().get(1).getTimestamp()));
	}
	
	@Test
	public void testUpdatedCreate() {
		ObjectHistoryEvent o = this.getBasicTestItem();
		
		o.updated(CreateEvent.builder().build());
		
		Assertions.assertThrows(IllegalArgumentException.class, ()->{
			o.updated(CreateEvent.builder().build());
		});
	}
	
	@Test
	public void testUpdatedUpdateFirst() {
		ObjectHistoryEvent o = this.getBasicTestItem();
		
		Assertions.assertThrows(IllegalArgumentException.class, ()->{
			o.updated(UpdateEvent.builder().build());
		});
	}
	
	@Test
	public void testGetLastUpdate() {
		ObjectHistoryEvent o = this.getBasicTestItem();
		
		o.updated(
			CreateEvent.builder().build()
		);
		o.updated(
			UpdateEvent.builder().build()
		);
		
		HistoryEvent event = o.lastHistoryEvent();
		
		assertEquals(o.getHistory().get(0), event);
	}
	
	@Test
	public void testGetLastUpdateTime() {
		ObjectHistoryEvent o = this.getBasicTestItem();
		
		o.updated(
			CreateEvent.builder().build()
		);
		o.updated(
			UpdateEvent.builder().build()
		);
		
		ZonedDateTime eventTime = o.lastHistoryEventTime();
		assertEquals(o.getHistory().get(0).getTimestamp(), eventTime);
	}
}