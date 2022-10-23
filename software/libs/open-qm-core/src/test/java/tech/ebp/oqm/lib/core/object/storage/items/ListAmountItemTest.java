package tech.ebp.oqm.lib.core.object.storage.items;

import tech.ebp.oqm.lib.core.units.OqmProvidedUnits;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class ListAmountItemTest extends BasicTest {
	
	public static ListAmountItem getLargeListAmountItem() {
		log.info("Generating large amount list item.");
		ListAmountItem item = new ListAmountItem();
		
		InventoryItemTest.fillCommon(item);
		
		List<ObjectId> storageIds = InventoryItemTest.getStorageList();
		
		for (ObjectId id : storageIds) {
			item.getStoredWrapperForStorage(id);
		}
		
		for (int i = 0; i < InventoryItemTest.NUM_STORED; i++) {
			AmountStored stored = new AmountStored(
				Quantities.getQuantity(
					RandomUtils.nextInt(0, 501),
					item.getUnit()
				)
			);
			InventoryItemTest.fillCommon(stored);
			stored.setAmount(Quantities.getQuantity(
				RandomUtils.nextInt(0, 501),
				item.getUnit()
			));
			
			item.getStorageMap().get(storageIds.get(RandomUtils.nextInt(0, storageIds.size()))).add(
				stored
			);
		}
		item.recalcValueOfStored();
		log.info("Done Generating large amount list item.");
		return item;
	}
	
	
	//TODO:: adding of different compatible units
	//TODO:: test with double values
	public static Stream<Arguments> getTotalArguments() {
		ObjectId id = ObjectId.get();
		return Stream.of(
			Arguments.of(
				new ListAmountItem(),
				Quantities.getQuantity(0, OqmProvidedUnits.UNIT)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored(0, OqmProvidedUnits.UNIT), true),
				Quantities.getQuantity(0, OqmProvidedUnits.UNIT)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true),
				Quantities.getQuantity(1, OqmProvidedUnits.UNIT)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true),
				Quantities.getQuantity(2, OqmProvidedUnits.UNIT)
			),
			Arguments.of(
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true),
				Quantities.getQuantity(3, OqmProvidedUnits.UNIT)
			)
			//			, // TODO:: when figure out double add quantities: https://github.com/unitsofmeasurement/indriya/issues/384
			//			Arguments.of(
			//				new ListAmountItem()
			//					.add(ObjectId.get(), new AmountStored(1.1, UnitUtils.UNIT), true)
			//					.add(ObjectId.get(), new AmountStored(1.2, UnitUtils.UNIT), true)
			//					.add(ObjectId.get(), new AmountStored(1.3, UnitUtils.UNIT), true),
			//				Quantities.getQuantity(3.6, UnitUtils.UNIT)
			//			),
			//			Arguments.of(
			//				new ListAmountItem()
			//					.add(ObjectId.get(), new AmountStored(1.1, UnitUtils.UNIT), true)
			//					.add(ObjectId.get(), new AmountStored(1.2, UnitUtils.UNIT), true)
			//					.add(ObjectId.get(), new AmountStored(1.3, UnitUtils.UNIT), true),
			//				Quantities.getQuantity(3.6, UnitUtils.UNIT)
			//			)
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
				new ListAmountItem()
					.add(ObjectId.get(), new AmountStored(OqmProvidedUnits.UNIT), true),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new ListAmountItem().add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new ListAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new ListAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(1L, OqmProvidedUnits.UNIT), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new ListAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(1.0, OqmProvidedUnits.UNIT), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new ListAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(1.0f, OqmProvidedUnits.UNIT), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new ListAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true),
				BigDecimal.valueOf(2.0)
			),
			Arguments.of(
				new ListAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true),
				BigDecimal.valueOf(3.0)
			),
			Arguments.of(
				new ListAmountItem()
					.setValuePerUnit(BigDecimal.ONE)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(ObjectId.get(), new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true)
					.add(id, new AmountStored(1, OqmProvidedUnits.UNIT), true),
				BigDecimal.valueOf(40.0)
			)
		);
	}
	
	public static Stream<Arguments> getExpiryArguments() {
		ObjectId id = ObjectId.get();
		return Stream.of(
			//TODO
			Arguments.of(
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
		item.recalcValueOfStored();
		assertEquals(
			valueExpected,
			item.getValueOfStored()
		);
	}
	
	
}