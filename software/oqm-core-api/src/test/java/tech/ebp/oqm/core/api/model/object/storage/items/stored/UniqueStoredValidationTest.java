package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import tech.ebp.oqm.core.api.model.testUtils.ObjectValidationTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class UniqueStoredValidationTest extends ObjectValidationTest<UniqueStored> {
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(new UniqueStored().setId(new ObjectId())),
			Arguments.of(
				new UniqueStored()
					.setCondition(50)
					.setExpires(LocalDateTime.now())
					.setImageIds(List.of(ObjectId.get()))
					.setConditionNotes(FAKER.lorem().paragraph())
					.setAttributes(Map.of("hello", "world"))
					.setKeywords(List.of("hello", "world"))
					.setId(new ObjectId())
			)
		);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new UniqueStored().setCondition(-1).setId(new ObjectId()),
				new HashMap<>() {{
					put("condition", "must be greater than or equal to 0");
				}}
			),
			Arguments.of(
				new UniqueStored().setCondition(101).setId(new ObjectId()),
				new HashMap<>() {{
					put("condition", "must be less than or equal to 100");
				}}
			)
		);
	}
}