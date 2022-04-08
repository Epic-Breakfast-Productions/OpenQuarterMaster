package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.TrackedStored;
import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackedItemTest extends BasicTest {
	
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
			item.valueOfStored()
		);
	}
}