package com.ebp.openQuarterMaster.lib.core.object.history;

import com.ebp.openQuarterMaster.lib.core.object.history.events.CreateEvent;
import com.ebp.openQuarterMaster.lib.core.object.history.events.UpdateEvent;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectSerializationTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class HistoriedSerializationTest extends ObjectSerializationTest<ObjectHistory> {
	
	HistoriedSerializationTest() {
		super(ObjectHistory.class);
	}
	
	public static ObjectHistory getHistory(int numEvents) {
		ObjectHistory output = new ObjectHistory();
		
		output.setId(ObjectId.get());
		output.setObjectId(ObjectId.get());
		
		output.updated(CreateEvent.builder().userId(ObjectId.get()).build());
		
		for (int i = 1; i < numEvents; i++) {
			output.updated(
				UpdateEvent.builder().userId(ObjectId.get()).description(FAKER.lorem().paragraph()).build()
			);
		}
		
		return output;
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new ObjectHistory()),
			Arguments.of(new ObjectHistory().setObjectId(ObjectId.get()).setId(ObjectId.get())),
			Arguments.of(getHistory(1)),
			Arguments.of(getHistory(10)),
			Arguments.of(getHistory(10_000))
		);
	}
	
}