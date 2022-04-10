package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ListAmountItemTest extends BasicTest {
	
	//TODO:: adding of different compatible units
	//TODO:: test with double values
	public static Stream<Arguments> getTotalArguments() {
		ObjectId id = ObjectId.get();
		return Stream.of(
			Arguments.of(
				new ListAmountItem(),
				Quantities.getQuantity(0, UnitUtils.UNIT)
			),
			Arguments.of(
				new ListAmountItem().add(ObjectId.get(), new AmountStored()),
				Quantities.getQuantity(0, UnitUtils.UNIT)
			),
			Arguments.of(
				new ListAmountItem().add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT))),
				Quantities.getQuantity(1, UnitUtils.UNIT)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT)))
					.add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT))),
				Quantities.getQuantity(2, UnitUtils.UNIT)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT)))
					.add(id, new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT)))
					.add(id, new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT))),
				Quantities.getQuantity(3, UnitUtils.UNIT)
			)
		);
	}
	
	//TODO:: test with double values
	public static Stream<Arguments> getValueArguments() {
		ObjectId id = ObjectId.get();
		return Stream.of(
			Arguments.of(
				new ListAmountItem(),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new ListAmountItem().add(ObjectId.get(), new AmountStored()),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new ListAmountItem().add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT))),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT)))
					.setValuePerUnit(BigDecimal.ONE),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1L, UnitUtils.UNIT)))
					.setValuePerUnit(BigDecimal.ONE),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1.0, UnitUtils.UNIT)))
					.setValuePerUnit(BigDecimal.ONE),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1.0f, UnitUtils.UNIT)))
					.setValuePerUnit(BigDecimal.ONE),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT)))
					.add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT)))
					.setValuePerUnit(BigDecimal.ONE),
				BigDecimal.valueOf(2.0)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT)))
					.add(id, new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT)))
					.add(id, new AmountStored().setAmount(Quantities.getQuantity(1, UnitUtils.UNIT)))
					.setValuePerUnit(BigDecimal.ONE),
				BigDecimal.valueOf(3.0)
			)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getTotalArguments")
	public void testTotalTest(ListAmountItem item, Quantity<?> quantityExpected) {
		assertEquals(
			quantityExpected,
			item.getTotal()
		);
	}
	
	@ParameterizedTest
	@MethodSource("getValueArguments")
	public void testGetValue(ListAmountItem item, BigDecimal valueExpected) {
		assertEquals(
			valueExpected,
			item.valueOfStored()
		);
	}
}