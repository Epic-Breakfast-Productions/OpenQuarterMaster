package tech.ebp.oqm.lib.core.object.storage.items;

import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class SimpleAmountItemTest extends BasicTest {
	
	public static SimpleAmountItem getLargeSimpleAmountItem() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		InventoryItemTest.fillCommon(item);
		
		for (int i = 0; i < InventoryItemTest.NUM_STORED; i++) {
			AmountStored stored = new AmountStored(
				Quantities.getQuantity(
					RandomUtils.nextInt(0, 501),
					item.getUnit()
				)
			);
			InventoryItemTest.fillCommon(stored);
			item.getStorageMap().put(
				ObjectId.get(),
				new SingleAmountStoredWrapper(stored)
			);
		}
		item.recalculateDerived();
		
		return item;
	}
	
	//TODO:: adding of different compatible units
	//TODO:: test with double values
	public static Stream<Arguments> getTotalArguments() {
		return Stream.of(
			Arguments.of(
				new SimpleAmountItem(),
				Quantities.getQuantity(0, UnitUtils.UNIT)
			),
			Arguments.of(
				new SimpleAmountItem().add(ObjectId.get(), new AmountStored(Quantities.getQuantity(0, UnitUtils.UNIT)), true),
				Quantities.getQuantity(0, UnitUtils.UNIT)
			),
			Arguments.of(
				new SimpleAmountItem().add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)), true),
				Quantities.getQuantity(1, UnitUtils.UNIT)
			),
			Arguments.of(
				new SimpleAmountItem()
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)), true)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)), true),
				Quantities.getQuantity(2, UnitUtils.UNIT)
			)
		);
	}
	
	//TODO:: test with double values
	public static Stream<Arguments> getValueArguments() {
		return Stream.of(
			Arguments.of(
				new SimpleAmountItem(),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(0, UnitUtils.UNIT)), true),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)), true),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1L, UnitUtils.UNIT)), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1.0, UnitUtils.UNIT)), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1.0f, UnitUtils.UNIT)), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)), true)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)), true),
				BigDecimal.valueOf(2.0)
			)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getTotalArguments")
	public void testTotalTest(SimpleAmountItem item, Quantity<?> quantityExpected) {
		item.recalcValueOfStored();
		assertEquals(
			quantityExpected,
			item.getTotal()
		);
	}
	
	@ParameterizedTest
	@MethodSource("getValueArguments")
	public void testGetValue(SimpleAmountItem item, BigDecimal valueExpected) {
		item.recalcValueOfStored();
		assertEquals(
			valueExpected,
			item.getValueOfStored()
		);
	}
	
	
	@Test
	public void testLargeItemTotalCalculation() {
		SimpleAmountItem item = getLargeSimpleAmountItem();
		
		StopWatch sw = StopWatch.createStarted();
		item.recalcTotal();
		sw.stop();
		
		log.info("Recalculating totals took {}", sw);
	}
	
	@Test
	public void testAddSimple() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		item.getStoredWrapperForStorage(storageId, true);
		
		item.add(storageId, new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)));
		
		assertEquals(Quantities.getQuantity(1, UnitUtils.UNIT), item.getTotal());
		assertEquals(Quantities.getQuantity(1, UnitUtils.UNIT), item.getStoredForStorage(storageId).getAmount());
	}
	
	@Test
	public void testAddTwo() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		item.getStoredWrapperForStorage(storageId, true);
		
		item.add(storageId, new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)));
		item.add(storageId, new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)));
		
		assertEquals(Quantities.getQuantity(2, UnitUtils.UNIT), item.getTotal());
		assertEquals(Quantities.getQuantity(2, UnitUtils.UNIT), item.getStoredForStorage(storageId).getAmount());
	}
	
	@Test
	public void testSubtract() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		item.add(storageId, new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)), true);
		
		item.subtract(storageId, new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)));
		
		assertEquals(Quantities.getQuantity(0, UnitUtils.UNIT), item.getTotal());
		assertEquals(Quantities.getQuantity(0, UnitUtils.UNIT), item.getStoredForStorage(storageId).getAmount());
	}
	
	@Test
	public void testSubtractNegative() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		item.add(storageId, new AmountStored(Quantities.getQuantity(0, UnitUtils.UNIT)), true);
		
		assertThrows(IllegalArgumentException.class, ()->{
			item.subtract(storageId, new AmountStored(Quantities.getQuantity(1, UnitUtils.UNIT)));
		});
		
	}
}