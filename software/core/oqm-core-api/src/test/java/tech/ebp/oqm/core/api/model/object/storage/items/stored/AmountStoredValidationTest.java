package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.testUtils.ObjectValidationTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AmountStoredValidationTest extends ObjectValidationTest<AmountStored> {
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(AmountStored.builder()
				.item(new ObjectId())
				.amount(Quantities.getQuantity(0, OqmProvidedUnits.UNIT))
				.build()
			)
		);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				AmountStored.builder()
					.item(new ObjectId())
					.amount(Quantities.getQuantity(0, OqmProvidedUnits.UNIT))
					.condition(-1)
					.build(),
				new HashMap<>() {{
					put("condition", "must be greater than or equal to 0");
				}}
			),
			Arguments.of(
				AmountStored.builder()
					.item(new ObjectId())
					.amount(Quantities.getQuantity(0, OqmProvidedUnits.UNIT))
					.condition(101)
					.build(),
				new HashMap<>() {{
					put("condition", "must be less than or equal to 100");
				}}
			),
			Arguments.of(
				AmountStored.builder()
					.item(new ObjectId())
					.amount(Quantities.getQuantity(0, AbstractUnit.ONE))
					.build(),
				new HashMap<>() {{
					put("amount", "Invalid unit. \"one\" not applicable for item storage.");
				}}
			)
		);
	}
}