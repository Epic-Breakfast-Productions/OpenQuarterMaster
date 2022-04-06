package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectSerializationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
class InventoryItemSerializationTest extends ObjectSerializationTest<InventoryItem> {
	
	protected InventoryItemSerializationTest() {
		super(InventoryItem.class);
	}
	
	private static List<AmountItem> getAmountItems() {
		return List.of(
			(AmountItem) new AmountItem().setName(FAKER.name().name()),
			(AmountItem) new AmountItem().setUnit(UnitUtils.ALLOWED_UNITS.get(0)).setName(FAKER.name().name())
		);
	}
	
	private static List<TrackedItem> getTrackedItems() {
		return List.of(
			(TrackedItem) new TrackedItem().setTrackedItemIdentifierName(FAKER.name().name()).setName(FAKER.name().name())
		);
	}
	
	
	public static Stream<Arguments> getObjects() {
		return Stream.concat(
						 getAmountItems().stream(),
						 getTrackedItems().stream()
					 )
					 .map(Arguments::of);
	}
	
}