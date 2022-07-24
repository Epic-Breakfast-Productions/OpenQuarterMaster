package com.ebp.openQuarterMaster.lib.core.history;

import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectSerializationTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class HistoryEventSerializationTest extends ObjectSerializationTest<HistoryEvent> {
	
	HistoryEventSerializationTest() {
		super(HistoryEvent.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new HistoryEvent(EventAction.CREATE, ObjectId.get())),
			Arguments.of(new AddRemoveItemHistoryEvent(EventAction.ADD, null, null, null))
		);
	}
	
}