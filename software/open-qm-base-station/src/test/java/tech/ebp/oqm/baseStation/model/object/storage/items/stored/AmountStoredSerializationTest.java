package tech.ebp.oqm.baseStation.model.object.storage.items.stored;

import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.baseStation.model.testUtils.ObjectSerializationTest;
import tech.ebp.oqm.baseStation.model.units.OqmProvidedUnits;

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