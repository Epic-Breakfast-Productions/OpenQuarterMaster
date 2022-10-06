package tech.ebp.oqm.lib.core.object.storage.items;

import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.Arguments;
import tech.units.indriya.quantity.Quantities;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class InventoryItemSerializationTest extends ObjectSerializationTest<InventoryItem> {
	
	protected InventoryItemSerializationTest() {
		super(InventoryItem.class);
	}
	
	private static List<SimpleAmountItem> getSimpleAmountItems() {
		return List.of(
			(SimpleAmountItem) new SimpleAmountItem()
								   .setName(FAKER.name().name())
								   .recalculateDerived(),
			(SimpleAmountItem) new SimpleAmountItem()
								   .add(
									   ObjectId.get(),
									   new AmountStored(Quantities.getQuantity(0, UnitUtils.UNIT)),
									   true
								   ).setName(FAKER.name().name())
								   .recalculateDerived(),
			(SimpleAmountItem) new SimpleAmountItem()
								   .setUnit(UnitUtils.ALLOWED_UNITS.get(0))
								   .setName(FAKER.name().name())
								   .recalculateDerived(),
			SimpleAmountItemTest.getLargeSimpleAmountItem()
		);
	}
	
	private static List<ListAmountItem> getListAmountItems() {
		return List.of(
			(ListAmountItem) new ListAmountItem().setName(FAKER.name().name()),
			(ListAmountItem) new ListAmountItem()
								 .add(ObjectId.get(), new AmountStored(Quantities.getQuantity(0, UnitUtils.UNIT)), true)
								 .setName(FAKER.name().name())
								 .recalculateDerived(),
			(ListAmountItem) new ListAmountItem()
								 .setUnit(UnitUtils.ALLOWED_UNITS.get(0))
								 .setName(FAKER.name().name())
								 .recalculateDerived(),
			ListAmountItemTest.getLargeListAmountItem()
		);
	}
	
	private static List<TrackedItem> getTrackedItems() {
		return List.of(
			(TrackedItem) new TrackedItem()
							  .setTrackedItemIdentifierName(FAKER.name().name())
							  .setName(FAKER.name().name())
							  .recalculateDerived(),
			(TrackedItem) new TrackedItem()
							  .setTrackedItemIdentifierName(FAKER.name().name())
							  .add(ObjectId.get(), new TrackedStored().setIdentifier(FAKER.business().creditCardNumber()), true)
							  .setName(FAKER.name().name())
							  .recalculateDerived(),
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