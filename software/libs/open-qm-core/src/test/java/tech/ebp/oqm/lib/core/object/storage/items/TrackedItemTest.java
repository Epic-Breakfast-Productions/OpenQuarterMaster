package tech.ebp.oqm.lib.core.object.storage.items;

import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class TrackedItemTest extends BasicTest {
	
	public static TrackedItem getLargeTrackedItem() {
		TrackedItem item = new TrackedItem();
		item.setTrackedItemIdentifierName("id");
		
		InventoryItemTest.fillCommon(item);
		
		List<ObjectId> storageIds = InventoryItemTest.getStorageList();
		
		for (ObjectId id : storageIds) {
			item.getStorageMap().put(
				id,
				new HashMap<>()
			);
		}
		
		for (int i = 0; i < InventoryItemTest.NUM_STORED; i++) {
			TrackedStored stored = new TrackedStored();
			InventoryItemTest.fillCommon(stored);
			stored.setIdentifyingDetails(FAKER.lorem().paragraph());
			stored.setValue(BigDecimal.valueOf(RandomUtils.nextDouble(5, 1_000_000)));
			
			item.getStorageMap().get(storageIds.get(RandomUtils.nextInt(0, storageIds.size()))).put(
				UUID.randomUUID().toString(),
				stored
			);
		}
		
		return item;
	}
	
	public static Stream<Arguments> getTotalArguments() {
		ObjectId id = ObjectId.get();
		return Stream.of(
			Arguments.of(
				new TrackedItem(),
				Quantities.getQuantity(0, UnitUtils.UNIT)
			),
			Arguments.of(
				new TrackedItem().add(ObjectId.get(), FAKER.name().name(), new TrackedStored()),
				Quantities.getQuantity(1, UnitUtils.UNIT)
			),
			Arguments.of(
				new TrackedItem()
					.add(ObjectId.get(), FAKER.name().name(), new TrackedStored())
					.add(ObjectId.get(), FAKER.name().name(), new TrackedStored()),
				Quantities.getQuantity(2, UnitUtils.UNIT)
			),
			Arguments.of(
				new TrackedItem()
					.add(ObjectId.get(), FAKER.name().name(), new TrackedStored())
					.add(id, FAKER.name().name(), new TrackedStored())
					.add(id, FAKER.name().name(), new TrackedStored()),
				Quantities.getQuantity(3, UnitUtils.UNIT)
			)
		);
	}
	
	public static Stream<Arguments> getValueArguments() {
		ObjectId id = ObjectId.get();
		return Stream.of(
			Arguments.of(
				new TrackedItem(),
				BigDecimal.ZERO
			),
			Arguments.of(
				new TrackedItem().add(ObjectId.get(), FAKER.name().name(), new TrackedStored().setValue(BigDecimal.ONE)),
				BigDecimal.ONE
			),
			Arguments.of(
				new TrackedItem()
					.add(ObjectId.get(), FAKER.name().name(), new TrackedStored().setValue(BigDecimal.ONE))
					.add(ObjectId.get(), FAKER.name().name(), new TrackedStored().setValue(BigDecimal.ONE)),
				BigDecimal.valueOf(2)
			),
			Arguments.of(
				new TrackedItem()
					.add(ObjectId.get(), FAKER.name().name(), new TrackedStored().setValue(BigDecimal.ONE))
					.add(id, FAKER.name().name(), new TrackedStored().setValue(BigDecimal.ONE))
					.add(id, FAKER.name().name(), new TrackedStored().setValue(BigDecimal.ONE)),
				BigDecimal.valueOf(3)
			)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getTotalArguments")
	public void testTotalTest(TrackedItem item, Quantity<?> quantityExpected) {
		assertEquals(
			quantityExpected,
			item.getTotal()
		);
	}
	
	@ParameterizedTest
	@MethodSource("getValueArguments")
	public void testGetValue(TrackedItem item, BigDecimal valueExpected) {
		assertEquals(
			valueExpected,
			item.getValueOfStored()
		);
	}
	
	@Test
	public void testLargeItemTotalCalculation() {
		TrackedItem item = getLargeTrackedItem();
		
		StopWatch sw = StopWatch.createStarted();
		item.recalcTotal();
		sw.stop();
		
		log.info("Recalculating totals took {}", sw);
	}
}