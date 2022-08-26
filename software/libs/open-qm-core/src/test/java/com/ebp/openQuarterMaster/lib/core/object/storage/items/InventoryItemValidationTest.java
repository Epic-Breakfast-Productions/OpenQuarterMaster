package com.ebp.openQuarterMaster.lib.core.object.storage.items;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.object.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectValidationTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
class InventoryItemValidationTest extends ObjectValidationTest<InventoryItem> {
	
	private static List<SimpleAmountItem> getSimpleAmountItemsValid() {
		return List.of(
			(SimpleAmountItem) new SimpleAmountItem().setName(FAKER.name().name()),
			(SimpleAmountItem) new SimpleAmountItem().setUnit(UnitUtils.ALLOWED_UNITS.get(0)).setName(FAKER.name().name())
		);
	}
	
	private static List<Map.Entry<SimpleAmountItem, Map<String, String>>> getSimpleAmountItemsInvalid() {
		SimpleAmountItem badUnitItem = (SimpleAmountItem) new SimpleAmountItem()
			.setName(FAKER.name().name());
		
		
		badUnitItem.getStorageMap().put(
			ObjectId.get(),
			new AmountStored().setAmount(Quantities.getQuantity(0, Units.GRAM))
		);
		
		
		return List.of(
			Map.entry(
				(SimpleAmountItem) new SimpleAmountItem().setName(""),
				new HashMap<>() {{
					put("name", "Name cannot be blank");
				}}
			),
			Map.entry(
				badUnitItem,
				new HashMap<>() {{
					put("", "Found 1 stored objects with units incompatible with the item's.");
				}}
			)
		);
	}
	
	private static List<ListAmountItem> getListAmountItemsValid() {
		return List.of(
			(ListAmountItem) new ListAmountItem().setName(FAKER.name().name()),
			(ListAmountItem) new ListAmountItem().setUnit(UnitUtils.ALLOWED_UNITS.get(0)).setName(FAKER.name().name())
		);
	}
	
	private static List<Map.Entry<ListAmountItem, Map<String, String>>> getListAmountItemsInvalid() {
		return List.of(
			Map.entry(
				(ListAmountItem) new ListAmountItem().setName(""),
				new HashMap<>() {{
					put("name", "Name cannot be blank");
				}}
			)
		);
	}
	
	private static List<TrackedItem> getTrackedItemsValid() {
		return List.of(
			(TrackedItem) new TrackedItem().setTrackedItemIdentifierName(FAKER.name().name()).setName(FAKER.name().name())
		);
	}
	
	private static List<Map.Entry<TrackedItem, Map<String, String>>> getTrackedItemsInvalid() {
		return List.of(
			Map.entry(
				(TrackedItem) new TrackedItem().setTrackedItemIdentifierName("").setName(FAKER.name().name()),
				new HashMap<>() {{
					put("trackedItemIdentifierName", "must not be blank");
				}}
			)
		);
	}
	
	
	public static Stream<Arguments> getValid() {
		return Stream.concat(
						 Stream.concat(
							 getSimpleAmountItemsValid().stream(),
							 getListAmountItemsValid().stream()
						 ),
						 getTrackedItemsValid().stream()
					 )
					 .map(Arguments::of);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.concat(
						 Stream.concat(
							 getSimpleAmountItemsInvalid().stream(),
							 getListAmountItemsInvalid().stream()
						 ),
						 getTrackedItemsInvalid().stream()
					 )
					 .map((cur)->{
						 return Arguments.of(cur.getKey(), cur.getValue());
					 });
	}
	
}