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
class HistoryEventSerializationTest extends ObjectSerializationTest<HistoryEvent> {
	
	HistoryEventSerializationTest() {
		super(HistoryEvent.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(HistoryEvent.builder().type(EventType.CREATE).build()),
			Arguments.of(HistoryEvent.builder().type(EventType.UPDATE).build()),
			Arguments.of(HistoryEvent.builder().type(EventType.DELETE).build()),
			Arguments.of(HistoryEvent.builder().type(EventType.ADD).build()),
			Arguments.of(HistoryEvent.builder().type(EventType.REMOVE).build()),
			Arguments.of(HistoryEvent.builder().type(EventType.ADD).userId(ObjectId.get()).build()),
			Arguments.of(HistoryEvent.builder().type(EventType.ADD).description(FAKER.lorem().paragraph()).build())
		);
	}
	
}