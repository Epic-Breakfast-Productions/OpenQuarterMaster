package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import systems.uom.common.USCustomary;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AmountStoredSerializationTest extends ObjectSerializationTest<AmountStored> {
	
	protected AmountStoredSerializationTest() {
		super(AmountStored.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new AmountStored(OqmProvidedUnits.UNIT)),
			Arguments.of(
				new AmountStored(5, OqmProvidedUnits.UNIT)
					.setExpires(LocalDateTime.now())
					.setCondition(50)
					.setConditionNotes(FAKER.lorem().paragraph())
					.setImageIds(List.of(ObjectId.get()))
					.setAttributes(Map.of("hello", "world"))
					.setKeywords(List.of("hello", "world"))
			
			),
			Arguments.of(
				new AmountStored(5, USCustomary.INCH)
					.setExpires(LocalDateTime.now())
					.setCondition(50)
					.setConditionNotes(FAKER.lorem().paragraph())
					.setImageIds(List.of(ObjectId.get()))
					.setAttributes(Map.of("hello", "world"))
					.setKeywords(List.of("hello", "world"))
			
			)
		);
	}
}