package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored;

import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.lib.core.units.OqmProvidedUnits;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;
import tech.units.indriya.quantity.Quantities;

import java.util.stream.Stream;

class ListAmountStoredWrapperSerializationTest extends ObjectSerializationTest<ListAmountStoredWrapper> {
	
	protected ListAmountStoredWrapperSerializationTest() {
		super(ListAmountStoredWrapper.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new ListAmountStoredWrapper().setParentUnit(OqmProvidedUnits.UNIT)),
			Arguments.of(new ListAmountStoredWrapper() {{
				setParentUnit(OqmProvidedUnits.UNIT);
				add(new AmountStored(Quantities.getQuantity(0, OqmProvidedUnits.UNIT)));
			}})
		);
	}
}