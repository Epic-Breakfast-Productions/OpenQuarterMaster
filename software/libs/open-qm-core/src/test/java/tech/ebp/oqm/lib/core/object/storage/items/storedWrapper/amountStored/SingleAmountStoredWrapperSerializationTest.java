package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored;

import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.lib.core.units.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;
import tech.units.indriya.quantity.Quantities;

import java.util.stream.Stream;

class SingleAmountStoredWrapperSerializationTest extends ObjectSerializationTest<SingleAmountStoredWrapper> {
	
	protected SingleAmountStoredWrapperSerializationTest() {
		super(SingleAmountStoredWrapper.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new SingleAmountStoredWrapper(new AmountStored(Quantities.getQuantity(0, UnitUtils.UNIT))))
		);
	}
}