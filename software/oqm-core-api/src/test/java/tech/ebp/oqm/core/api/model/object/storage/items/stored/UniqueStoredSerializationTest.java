package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class UniqueStoredSerializationTest extends ObjectSerializationTest<UniqueStored> {
	
	protected UniqueStoredSerializationTest() {
		super(UniqueStored.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new UniqueStored().setId(new ObjectId())),
			Arguments.of(
				new UniqueStored()
					.setCondition(50)
					.setExpires(LocalDateTime.now())
					.setConditionNotes(FAKER.lorem().paragraph())
					.setImageIds(List.of(ObjectId.get()))
					.setAttributes(Map.of("hello", "world"))
					.setKeywords(List.of("hello", "world"))
					.setId(new ObjectId())
			)
		);
	}
}