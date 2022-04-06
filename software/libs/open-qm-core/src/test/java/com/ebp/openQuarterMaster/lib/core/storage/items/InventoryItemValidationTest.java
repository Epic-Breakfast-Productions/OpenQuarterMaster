package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectValidationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
class InventoryItemValidationTest extends ObjectValidationTest<InventoryItem> {
	
	private static List<AmountItem> getAmountItemsValid() {
		return List.of(
			(AmountItem) new AmountItem().setName(FAKER.name().name()),
			(AmountItem) new AmountItem().setUnit(UnitUtils.ALLOWED_UNITS.get(0)).setName(FAKER.name().name())
		);
	}
	
	private static List<Map.Entry<AmountItem, Map<String, String>>> getAmountItemsInvalid() {
		return List.of(
			Map.entry(
				(AmountItem) new AmountItem().setName(""),
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
						 getAmountItemsValid().stream(),
						 getTrackedItemsValid().stream()
					 )
					 .map(Arguments::of);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.concat(
						 getAmountItemsInvalid().stream(),
						 getTrackedItemsInvalid().stream()
					 )
					 .map((cur)->{
						 return Arguments.of(cur.getKey(), cur.getValue());
					 });
	}
	
}