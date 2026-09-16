package tech.ebp.oqm.lib.core.object.storage.items.stored;

import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class TrackedStoredSerializationTest extends ObjectSerializationTest<TrackedStored> {
	
	protected TrackedStoredSerializationTest() {
		super(TrackedStored.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new TrackedStored().setIdentifier(FAKER.idNumber().peselNumber())),
			Arguments.of(
				new TrackedStored()
					.setIdentifier(FAKER.idNumber().peselNumber())
					.setCondition(50)
					.setConditionNotes(FAKER.lorem().paragraph())
					.setAttributes(Map.of("hello", "world"))
					.setExpires(LocalDateTime.now())
					.setKeywords(List.of("hello", "world"))
					.setImageIds(List.of(ObjectId.get()))
			)
		);
	}
}