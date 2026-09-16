package tech.ebp.oqm.core.api.model.object.storage.items;

import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.testUtils.ObjectValidationTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;

import java.util.HashMap;
import java.util.stream.Stream;

@Slf4j
class InventoryItemValidationTest extends ObjectValidationTest<InventoryItem> {

	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(InventoryItem.builder()
				.storageType(StorageType.UNIQUE_SINGLE)
				.name(FAKER.food().fruit())
				.unit(OqmProvidedUnits.UNIT)
				.build())
		);
	}

	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				InventoryItem.builder()
					.storageType(StorageType.UNIQUE_SINGLE)
					.name("")
					.unit(OqmProvidedUnits.UNIT)
					.build(),
				new HashMap<>() {{
					put("name", "Name cannot be blank");
				}}
			)
			//TODO:: add more
		);
	}
//


}