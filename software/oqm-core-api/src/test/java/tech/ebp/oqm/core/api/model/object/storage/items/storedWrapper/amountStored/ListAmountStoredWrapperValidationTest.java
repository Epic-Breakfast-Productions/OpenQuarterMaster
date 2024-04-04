package tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.amountStored;

import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.testUtils.ObjectValidationTest;
import tech.units.indriya.unit.Units;

import java.util.HashMap;
import java.util.stream.Stream;

class ListAmountStoredWrapperValidationTest extends ObjectValidationTest<ListAmountStoredWrapper> {
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(new ListAmountStoredWrapper(OqmProvidedUnits.UNIT))
		);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new ListAmountStoredWrapper(Units.DAY),
				new HashMap<>() {{
					put("parentUnit", "Invalid unit. day not applicable for item storage.");
				}}
			)
		);
	}
}