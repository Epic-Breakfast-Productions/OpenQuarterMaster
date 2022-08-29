package com.ebp.openQuarterMaster.lib.core.object.storage.items;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.object.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.object.storage.items.stored.TrackedStored;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectSerializationTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static com.ebp.openQuarterMaster.lib.core.object.storage.items.ListAmountItemTest.getLargeListAmountItem;
import static com.ebp.openQuarterMaster.lib.core.object.storage.items.SimpleAmountItemTest.getLargeSimpleAmountItem;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class InventoryItemSerializationTest extends ObjectSerializationTest<InventoryItem> {
	
	protected InventoryItemSerializationTest() {
		super(InventoryItem.class);
	}
	
	private static List<SimpleAmountItem> getSimpleAmountItems() {
		return List.of(
			(SimpleAmountItem) new SimpleAmountItem().setName(FAKER.name().name()),
			(SimpleAmountItem) new SimpleAmountItem()
								   .addNewStored(ObjectId.get(), new AmountStored()).setName(FAKER.name().name()),
			(SimpleAmountItem) new SimpleAmountItem().setUnit(UnitUtils.ALLOWED_UNITS.get(0)).setName(FAKER.name().name()),
			getLargeSimpleAmountItem()
		);
	}
	
	private static List<ListAmountItem> getListAmountItems() {
		return List.of(
			(ListAmountItem) new ListAmountItem().setName(FAKER.name().name()),
			(ListAmountItem) new ListAmountItem()
				.add(ObjectId.get(), new AmountStored()).setName(FAKER.name().name()),
			(ListAmountItem) new ListAmountItem().setUnit(UnitUtils.ALLOWED_UNITS.get(0)).setName(FAKER.name().name()),
			getLargeListAmountItem()
		);
	}
	
	private static List<TrackedItem> getTrackedItems() {
		return List.of(
			(TrackedItem) new TrackedItem().setTrackedItemIdentifierName(FAKER.name().name()).setName(FAKER.name().name()),
			(TrackedItem) new TrackedItem()
				.add(ObjectId.get(), "1234", new TrackedStored())
				.setTrackedItemIdentifierName(FAKER.name().name())
				.setName(FAKER.name().name()),
			TrackedItemTest.getLargeTrackedItem()
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