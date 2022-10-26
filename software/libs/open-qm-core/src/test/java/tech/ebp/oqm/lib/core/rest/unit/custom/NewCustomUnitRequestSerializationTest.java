package tech.ebp.oqm.lib.core.rest.unit.custom;

import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;
import tech.ebp.oqm.lib.core.units.OqmProvidedUnits;
import tech.ebp.oqm.lib.core.units.UnitCategory;
import tech.ebp.oqm.lib.core.units.ValidUnitDimension;

import java.math.BigDecimal;
import java.util.stream.Stream;

class NewCustomUnitRequestSerializationTest extends ObjectSerializationTest<NewCustomUnitRequest> {
	
	protected NewCustomUnitRequestSerializationTest() {
		super(NewCustomUnitRequest.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				NewBaseCustomUnitRequest
					.builder()
					.unitCategory(UnitCategory.Number)
					.symbol(FAKER.food().dish())
					.name(FAKER.food().fruit())
					.dimension(ValidUnitDimension.length)
					.build()
			),
			Arguments.of(
				NewDerivedCustomUnitRequest
					.builder()
					.unitCategory(UnitCategory.Number)
					.symbol(FAKER.food().dish())
					.name(FAKER.food().fruit())
					.baseUnit(OqmProvidedUnits.UNIT)
					.numPerBaseUnit(new BigDecimal("10"))
					.deriveType(NewDerivedCustomUnitRequest.DeriveType.multiply)
					.build()
			),
			Arguments.of(
				NewDerivedCustomUnitRequest
					.builder()
					.unitCategory(UnitCategory.Number)
					.symbol(FAKER.food().dish())
					.name(FAKER.food().fruit())
					.baseUnit(OqmProvidedUnits.UNIT)
					.numPerBaseUnit(new BigDecimal("-10"))
					.deriveType(NewDerivedCustomUnitRequest.DeriveType.divide)
					.build()
			)
		);
	}
}