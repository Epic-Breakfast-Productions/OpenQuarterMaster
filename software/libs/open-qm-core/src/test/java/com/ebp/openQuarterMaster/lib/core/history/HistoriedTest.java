package com.ebp.openQuarterMaster.lib.core.history;

import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class HistoriedTest extends BasicTest {
	
	public ObjectHistory getBasicTestItem() {
		return new ObjectHistory();
	}
	
	@Test
	public void testUpdated() {
		ObjectHistory o = this.getBasicTestItem();
		
		assertTrue(o.getHistory().isEmpty());
		assertEquals(0, o.getHistory().size());
		
		ObjectHistory o2 = o.updated(
			new HistoryEvent(
				EventAction.CREATE,
				ObjectId.get()
			)
		);
		assertSame(o, o2);
		assertEquals(1, o.getHistory().size());
		
		o2 = o.updated(
			new HistoryEvent(
				EventAction.UPDATE,
				ObjectId.get()
			)
		);
		
		assertTrue(o.getHistory().get(0).getTimestamp().isAfter(o.getHistory().get(1).getTimestamp()));
	}
	
	@Test
	public void testUpdatedCreate() {
		ObjectHistory o = this.getBasicTestItem();
		
		o.updated(new HistoryEvent(
					  EventAction.CREATE,
					  ObjectId.get()
				  )
		);
		
		Assertions.assertThrows(IllegalArgumentException.class, ()->{
			o.updated(new HistoryEvent(
				EventAction.CREATE,
				ObjectId.get()
			));
		});
	}
	
	@Test
	public void testUpdatedUpdateFirst() {
		ObjectHistory o = this.getBasicTestItem();
		
		Assertions.assertThrows(IllegalArgumentException.class, ()->{
			o.updated(new HistoryEvent(
				EventAction.UPDATE,
				ObjectId.get()
			));
		});
	}
	
	@Test
	public void testGetLastUpdate() {
		ObjectHistory o = this.getBasicTestItem();
		
		o.updated(
			new HistoryEvent(
				EventAction.CREATE,
				ObjectId.get()
			)
		);
		o.updated(
			new HistoryEvent(
				EventAction.UPDATE,
				ObjectId.get()
			)
		);
		
		HistoryEvent event = o.lastHistoryEvent();
		
		assertEquals(o.getHistory().get(0), event);
	}
	
	@Test
	public void testGetLastUpdateTime() {
		ObjectHistory o = this.getBasicTestItem();
		
		o.updated(
			new HistoryEvent(
				EventAction.CREATE,
				ObjectId.get()
			)
		);
		o.updated(
			new HistoryEvent(
				EventAction.UPDATE,
				ObjectId.get()
			)
		);
		
		ZonedDateTime eventTime = o.lastHistoryEventTime();
		assertEquals(o.getHistory().get(0).getTimestamp(), eventTime);
	}
}