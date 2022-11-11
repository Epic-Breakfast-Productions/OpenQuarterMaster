package tech.ebp.oqm.lib.core.object.history;

import tech.ebp.oqm.lib.core.object.history.events.CreateEvent;
import tech.ebp.oqm.lib.core.object.history.events.UpdateEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;
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
		
		output.updated(CreateEvent.builder().entityId(ObjectId.get()).entityType(InteractingEntityType.USER).build());
		
		for (int i = 1; i < numEvents; i++) {
			output.updated(
				UpdateEvent.builder()
						   .entityId(ObjectId.get())
						   .entityType(InteractingEntityType.USER)
						   .description(FAKER.lorem().paragraph())
						   .build()
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