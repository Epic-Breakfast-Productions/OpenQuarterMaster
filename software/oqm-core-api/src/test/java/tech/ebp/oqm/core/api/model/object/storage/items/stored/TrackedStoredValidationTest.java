package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import tech.ebp.oqm.core.api.model.testUtils.ObjectValidationTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class TrackedStoredValidationTest extends ObjectValidationTest<TrackedStored> {
	
	public static Stream<Arguments> getValid() {
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
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new TrackedStored().setIdentifier(""),
				new HashMap<>() {{
					put("identifier", "must not be blank");
				}}
			),
			Arguments.of(
				new TrackedStored(),
				new HashMap<>() {{
					put("identifier", "must not be blank");
				}}
			),
			Arguments.of(
				new TrackedStored().setIdentifier(FAKER.idNumber().peselNumber()).setCondition(-1),
				new HashMap<>() {{
					put("condition", "must be greater than or equal to 0");
				}}
			),
			Arguments.of(
				new TrackedStored().setIdentifier(FAKER.idNumber().peselNumber()).setCondition(101),
				new HashMap<>() {{
					put("condition", "must be less than or equal to 100");
				}}
			),
			Arguments.of(
				new TrackedStored().setIdentifier(FAKER.idNumber().peselNumber()).setValue(BigDecimal.valueOf(-1)),
				new HashMap<>() {{
					put("value", "must be greater than or equal to 0.0");
				}}
			)
		);
	}
}