package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import systems.uom.common.USCustomary;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.units.indriya.quantity.Quantities;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AmountStoredSerializationTest extends ObjectSerializationTest<AmountStored> {
	
	protected AmountStoredSerializationTest() {
		super(AmountStored.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				AmountStored.builder()
					.item(new ObjectId())
					.amount(Quantities.getQuantity(0, OqmProvidedUnits.UNIT))
					.build()
			),
			Arguments.of(
				AmountStored.builder()
					.item(new ObjectId())
					.amount(Quantities.getQuantity(0, OqmProvidedUnits.UNIT))
					.expires(ZonedDateTime.now())
					.build()
			)
				//TODO:: more
		);
	}
}