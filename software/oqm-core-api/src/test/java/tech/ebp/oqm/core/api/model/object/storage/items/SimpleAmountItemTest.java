package tech.ebp.oqm.core.api.model.object.storage.items;

import tech.ebp.oqm.core.api.model.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.exception.UnsupportedStoredOperationException;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class SimpleAmountItemTest extends InventoryItemTest {
	
	public static SimpleAmountItem getLargeSimpleAmountItem() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		fillCommon(item);
		
		for (int i = 0; i < NUM_STORED; i++) {
			AmountStored stored = new AmountStored(
				Quantities.getQuantity(
					RandomUtils.nextInt(0, 501),
					item.getUnit()
				)
			);
			fillCommon(stored);
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
				Quantities.getQuantity(0.0, OqmProvidedUnits.UNIT)
			),
			Arguments.of(
				new SimpleAmountItem().add(ObjectId.get(), new AmountStored(Quantities.getQuantity(0, OqmProvidedUnits.UNIT)), true),
				Quantities.getQuantity(0, OqmProvidedUnits.UNIT)
			),
			Arguments.of(
				new SimpleAmountItem().add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true),
				Quantities.getQuantity(1, OqmProvidedUnits.UNIT)
			),
			Arguments.of(
				new SimpleAmountItem()
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true),
				Quantities.getQuantity(2, OqmProvidedUnits.UNIT)
			)
			//			, //TODO:: https://github.com/unitsofmeasurement/indriya/issues/384
			//			Arguments.of(
			//				new SimpleAmountItem()
			//					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1.1, UnitUtils.UNIT)), true)
			//					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1.2, UnitUtils.UNIT)), true),
			//				Quantities.getQuantity(2.3, UnitUtils.UNIT)
			//			)
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
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(0, OqmProvidedUnits.UNIT)), true),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1L, OqmProvidedUnits.UNIT)), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1.0, OqmProvidedUnits.UNIT)), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1.0f, OqmProvidedUnits.UNIT)), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new SimpleAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true)
					.add(ObjectId.get(), new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true),
				BigDecimal.valueOf(2.0)
			)
		);
	}
	
	public static Stream<Arguments> getExpiryArguments() {
		//TODO:: update lists to hold verifyable information, do this for other item types. More cases
		LocalDateTime notExpired = getNotExpiredExpiryDate();
		LocalDateTime expired = getExpiredExpiryDate();
		Duration expiryWarnThreshold = getExpiryWarnThreshold();
		
		ObjectId idOne = ObjectId.get();
		ObjectId idTwo = ObjectId.get();
		ObjectId idThree = ObjectId.get();
		return Stream.of(
			Arguments.of(
				new ListAmountItem() {{
					add(idOne, new AmountStored(OqmProvidedUnits.UNIT), true);
				}},
				List.of(),
				List.of()
			),
			Arguments.of(
				new ListAmountItem() {{
					add(idOne, (AmountStored) new AmountStored(OqmProvidedUnits.UNIT).setExpires(notExpired), true);
				}},
				List.of(),
				List.of()
			),
			Arguments.of(
				new ListAmountItem() {{
					add(idOne, (AmountStored) new AmountStored(OqmProvidedUnits.UNIT).setExpires(expired), true);
				}},
				List.of(new AmountStored().setExpires(expired)),
				List.of()
			),
			Arguments.of(
				new ListAmountItem() {{
					add(idOne, (AmountStored) new AmountStored(OqmProvidedUnits.UNIT).setExpires(notExpired), true);
				}},
				List.of(),
				List.of()
			),
			Arguments.of(
				new ListAmountItem() {{
					setExpiryWarningThreshold(expiryWarnThreshold);
					add(idOne, (AmountStored) new AmountStored(OqmProvidedUnits.UNIT).setExpires(notExpired), true);
				}},
				List.of(),
				List.of(new AmountStored().setExpires(notExpired))
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
		
		item.add(storageId, new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)));
		
		assertEquals(Quantities.getQuantity(1, OqmProvidedUnits.UNIT), item.getTotal());
		assertEquals(Quantities.getQuantity(1, OqmProvidedUnits.UNIT), item.getStoredForStorage(storageId).getAmount());
	}
	
	@Test
	public void testAddSimpleWithId() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		UUID storedId = item.getStoredWrapperForStorage(storageId, true).getStored().getId();
		
		item.add(storageId, storedId, new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true);
		
		assertEquals(Quantities.getQuantity(1, OqmProvidedUnits.UNIT), item.getTotal());
		assertEquals(Quantities.getQuantity(1, OqmProvidedUnits.UNIT), item.getStoredForStorage(storageId).getAmount());
	}
	
	@Test
	public void testAddSimpleWithWrongId() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		item.getStoredWrapperForStorage(storageId, true);
		
		assertThrows(
			UnsupportedStoredOperationException.class,
			()->item.add(storageId, UUID.randomUUID(), new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true)
		);
		
		assertEquals(Quantities.getQuantity(0, OqmProvidedUnits.UNIT), item.getTotal());
		assertEquals(Quantities.getQuantity(0, OqmProvidedUnits.UNIT), item.getStoredForStorage(storageId).getAmount());
	}
	
	@Test
	public void testAddTwo() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		item.getStoredWrapperForStorage(storageId, true);
		
		item.add(storageId, new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)));
		item.add(storageId, new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)));
		
		assertEquals(Quantities.getQuantity(2, OqmProvidedUnits.UNIT), item.getTotal());
		assertEquals(Quantities.getQuantity(2, OqmProvidedUnits.UNIT), item.getStoredForStorage(storageId).getAmount());
	}
	
	@Test
	public void testSubtract() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		item.add(storageId, new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true);
		
		item.subtract(storageId, new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)));
		
		assertEquals(Quantities.getQuantity(0, OqmProvidedUnits.UNIT), item.getTotal());
		assertEquals(Quantities.getQuantity(0, OqmProvidedUnits.UNIT), item.getStoredForStorage(storageId).getAmount());
	}
	
	@Test
	public void testSubtractWithId() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		item.add(storageId, new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true);
		UUID storedId = item.getStoredForStorage(storageId).getId();
		
		item.subtract(storageId, storedId, new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)));
		
		assertEquals(Quantities.getQuantity(0, OqmProvidedUnits.UNIT), item.getTotal());
		assertEquals(Quantities.getQuantity(0, OqmProvidedUnits.UNIT), item.getStoredForStorage(storageId).getAmount());
	}
	
	@Test
	public void testSubtractWithWrongId() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		item.add(storageId, new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)), true);
		
		assertThrows(
			UnsupportedStoredOperationException.class,
			()->item.subtract(storageId, UUID.randomUUID(), new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)))
		);
		
		assertEquals(Quantities.getQuantity(1, OqmProvidedUnits.UNIT), item.getTotal());
		assertEquals(Quantities.getQuantity(1, OqmProvidedUnits.UNIT), item.getStoredForStorage(storageId).getAmount());
	}
	
	@Test
	public void testSubtractNegative() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		ObjectId storageId = ObjectId.get();
		
		item.add(storageId, new AmountStored(Quantities.getQuantity(0, OqmProvidedUnits.UNIT)), true);
		
		assertThrows(IllegalArgumentException.class, ()->{
			item.subtract(storageId, new AmountStored(Quantities.getQuantity(1, OqmProvidedUnits.UNIT)));
		});
		
	}
	
	//TODO:: mode low stock tests to super class
	@Test
	//	@Override
	public void testLowStockEmpty() {
		SimpleAmountItem item = new SimpleAmountItem();
		
		List<ItemLowStockEvent> events = item.updateLowStockState();
		
		assertTrue(events.isEmpty());
		assertFalse(item.getNotificationStatus().isLowStock());
		assertEquals(0, item.getNumLowStock());
	}
	
	@Test
	//	@Override
	public void testLowStockNoThreshold() {
		SimpleAmountItem item = new SimpleAmountItem();
		item.add(ObjectId.get(), new AmountStored(5, OqmProvidedUnits.UNIT));
		
		List<ItemLowStockEvent> events = item.updateLowStockState();
		
		assertTrue(events.isEmpty());
		assertFalse(item.getNotificationStatus().isLowStock());
		assertEquals(0, item.getNumLowStock());
	}
	
	@Test
	//	@Override
	public void testTotalLowStock() {
		SimpleAmountItem item = new SimpleAmountItem();
		item.add(ObjectId.get(), new AmountStored(5, OqmProvidedUnits.UNIT));
		item.setLowStockThreshold(Quantities.getQuantity(6, OqmProvidedUnits.UNIT));
		
		List<ItemLowStockEvent> events = item.updateLowStockState();
		
		assertEquals(1, events.size());
		assertTrue(item.getNotificationStatus().isLowStock());
		assertEquals(1, item.getNumLowStock());
	}
	
	@Test
	//	@Override
	public void testTotalLowStockReturned() {
		SimpleAmountItem item = new SimpleAmountItem();
		item.add(ObjectId.get(), new AmountStored(5, OqmProvidedUnits.UNIT));
		item.setLowStockThreshold(Quantities.getQuantity(6, OqmProvidedUnits.UNIT));
		
		List<ItemLowStockEvent> events = item.updateLowStockState();
		
		assertEquals(1, events.size());
		assertTrue(item.getNotificationStatus().isLowStock());
		assertEquals(1, item.getNumLowStock());
		
		events = item.updateLowStockState();
		
		assertTrue(events.isEmpty());
		assertTrue(item.getNotificationStatus().isLowStock());
		assertEquals(1, item.getNumLowStock());
		
		item.add(ObjectId.get(), new AmountStored(5, OqmProvidedUnits.UNIT));
		
		
		events = item.updateLowStockState();
		
		assertTrue(events.isEmpty());
		assertFalse(item.getNotificationStatus().isLowStock());
		assertEquals(0, item.getNumLowStock());
	}
}