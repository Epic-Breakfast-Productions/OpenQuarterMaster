package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.TrackedStored;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectSerializationTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
class InventoryItemSerializationTest extends ObjectSerializationTest<InventoryItem> {
	
	protected InventoryItemSerializationTest() {
		super(InventoryItem.class);
	}
	
	private static List<SimpleAmountItem> getSimpleAmountItems() {
		return List.of(
			(SimpleAmountItem) new SimpleAmountItem().setName(FAKER.name().name()),
			(SimpleAmountItem) new SimpleAmountItem()
				.add(ObjectId.get(), new AmountStored()).setName(FAKER.name().name()),
			(SimpleAmountItem) new SimpleAmountItem().setUnit(UnitUtils.ALLOWED_UNITS.get(0)).setName(FAKER.name().name())
		);
	}
	
	private static List<ListAmountItem> getListAmountItems() {
		return List.of(
			(ListAmountItem) new ListAmountItem().setName(FAKER.name().name()),
			(ListAmountItem) new ListAmountItem()
				.add(ObjectId.get(), new AmountStored()).setName(FAKER.name().name()),
			(ListAmountItem) new ListAmountItem().setUnit(UnitUtils.ALLOWED_UNITS.get(0)).setName(FAKER.name().name())
		);
	}
	
	private static List<TrackedItem> getTrackedItems() {
		return List.of(
			(TrackedItem) new TrackedItem().setTrackedItemIdentifierName(FAKER.name().name()).setName(FAKER.name().name()),
			(TrackedItem) new TrackedItem()
				.add(ObjectId.get(), "1234", new TrackedStored())
				.setTrackedItemIdentifierName(FAKER.name().name())
				.setName(FAKER.name().name())
		);
	}
	
	
	public static Stream<Arguments> getObjects() {
		return Stream.concat(
						 Stream.concat(
							 getSimpleAmountItems().stream(),
							 getListAmountItems().stream()
						 ),
						 getTrackedItems().stream()
					 )
					 .map(Arguments::of);
	}
	
}