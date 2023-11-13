package tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.amountStored;

import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.baseStation.model.units.OqmProvidedUnits;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.baseStation.model.testUtils.ObjectSerializationTest;
import tech.units.indriya.quantity.Quantities;

import java.util.stream.Stream;

class SingleAmountStoredWrapperSerializationTest extends ObjectSerializationTest<SingleAmountStoredWrapper> {
	
	protected SingleAmountStoredWrapperSerializationTest() {
		super(SingleAmountStoredWrapper.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new SingleAmountStoredWrapper(new AmountStored(Quantities.getQuantity(0, OqmProvidedUnits.UNIT))))
		);
	}
}