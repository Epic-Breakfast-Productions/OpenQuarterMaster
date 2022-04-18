package com.ebp.openQuarterMaster.lib.core.storage.items.stored;

import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectValidationTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AmountStoredValidationTest extends ObjectValidationTest<AmountStored> {
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(new AmountStored()),
			Arguments.of(
				new AmountStored()
					.setCondition(50)
					.setConditionNotes(FAKER.lorem().paragraph())
					.setAttributes(Map.of("hello", "world"))
					.setExpires(ZonedDateTime.now())
					.setKeywords(List.of("hello", "world"))
					.setImageIds(List.of(ObjectId.get()))
			)
		);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new AmountStored().setCondition(-1),
				new HashMap<>() {{
					put("condition", "must be greater than or equal to 0");
				}}
			),
			Arguments.of(
				new AmountStored().setCondition(101),
				new HashMap<>() {{
					put("condition", "must be less than or equal to 100");
				}}
			),
			Arguments.of(
				new AmountStored().setAmount(Quantities.getQuantity(0.0, AbstractUnit.ONE)),
				new HashMap<>() {{
					put("amount", "Invalid unit. \"one\" not applicable for item storage.");
				}}
			)
		);
	}
}