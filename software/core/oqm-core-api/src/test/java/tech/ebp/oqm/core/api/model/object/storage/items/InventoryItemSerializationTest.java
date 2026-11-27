package tech.ebp.oqm.core.api.model.object.storage.items;

import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.GenericIdentifier;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class InventoryItemSerializationTest extends ObjectSerializationTest<InventoryItem> {

	protected InventoryItemSerializationTest() {
		super(InventoryItem.class);
	}

	private static InventoryItem buildFilled(int fillFactor){
		return InventoryItem.builder()
			.storageType(StorageType.UNIQUE_SINGLE)
			.name(FAKER.food().fruit())
			.description(FAKER.lorem().paragraph())
			.storageBlocks(new ArrayList<>() {{
				for (int i = 0; i < fillFactor; i++) {
					this.add(StorageBlockSettings.builder()
						.notes(FAKER.lorem().paragraph())
						.storageBlock(ObjectId.get())
						.build());
				}
			}})
			.categories(new ArrayList<>() {{
				for (int i = 0; i < fillFactor; i++) {
					this.add(ObjectId.get());
				}
			}})
			.imageIds(new ArrayList<>() {{
				for (int i = 0; i < fillFactor; i++) {
					this.add(ObjectId.get());
				}
			}})
			.attachedFiles(new HashSet<>() {{
				for (int i = 0; i < fillFactor; i++) {
					this.add(ObjectId.get());
				}
			}})
			.identifiers(new LinkedHashSet<>(){{
				for (int i = 0; i < fillFactor; i++) {
					this.add(GenericIdentifier.builder()
						.label(i + FAKER.bluey().character())
						.value(FAKER.idNumber().valid())
						.build());
				}
			}})
			//TODO:: stats
			.unit(OqmProvidedUnits.UNIT)
			.build();
	}

	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				InventoryItem.builder()
					.storageType(StorageType.UNIQUE_SINGLE)
					.name(FAKER.food().fruit())
					.unit(OqmProvidedUnits.UNIT)
					.build()
			),
			Arguments.of(buildFilled(1)),
			Arguments.of(buildFilled(250)),
			Arguments.of(buildFilled(1_000)),
			Arguments.of(buildFilled(10_000)),
			Arguments.of(buildFilled(100_000))
		);
	}

}