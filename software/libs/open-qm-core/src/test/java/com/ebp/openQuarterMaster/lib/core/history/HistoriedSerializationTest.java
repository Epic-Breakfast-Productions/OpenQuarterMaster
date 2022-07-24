package com.ebp.openQuarterMaster.lib.core.history;

import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectSerializationTest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.Arguments;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class HistoriedSerializationTest extends ObjectSerializationTest<HistoriedSerializationTest.TestHistoried> {
	
	HistoriedSerializationTest() {
		super(TestHistoried.class);
	}
	
	@NoArgsConstructor
	public static class TestHistoried extends ObjectHistory {
	
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new TestHistoried()),
			Arguments.of(
				new TestHistoried() {{
					int numEvents = 10_000;
					
					this.updated(new HistoryEvent(
									 EventAction.CREATE,
									 ObjectId.get(),
									 ZonedDateTime.now(),
									 FAKER.lorem().sentence()
								 )
					);
					
					for (int i = 1; i < numEvents; i++) {
						this.updated(new HistoryEvent(
							EventAction.UPDATE,
							ObjectId.get(),
							ZonedDateTime.now(),
							FAKER.lorem().sentence()
						));
					}
				}}
			)
		);
	}
	
}