package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class UniqueStoredSerializationTest extends ObjectSerializationTest<UniqueStored> {
	
	protected UniqueStoredSerializationTest() {
		super(UniqueStored.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				UniqueStored.builder().id(new ObjectId()).item(new ObjectId()).build()
			),
			Arguments.of(
				UniqueStored.builder()
					.item(new ObjectId())
					.condition(50)
					.expires(ZonedDateTime.now())
					.conditionNotes(FAKER.lorem().paragraph())
					.imageIds(List.of(ObjectId.get()))
					.attributes(Map.of("hello", "world"))
					.keywords(List.of("hello", "world"))
					.id(new ObjectId())
					.build()
			)
		);
	}
}