package tech.ebp.oqm.core.api.model.object.storage.items;

import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;

import java.util.stream.Stream;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class InventoryItemSerializationTest extends ObjectSerializationTest<InventoryItem> {
	
	protected InventoryItemSerializationTest() {
		super(InventoryItem.class);
	}

	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				InventoryItem.builder()
					.storageType(StorageType.UNIQUE_SINGLE)
					.name(FAKER.food().fruit())
					.unit(OqmProvidedUnits.UNIT)
					.build()
			)
			//TODO:: more
		);
	}
	
}