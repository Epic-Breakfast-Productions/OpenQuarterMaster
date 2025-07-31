package tech.ebp.oqm.core.api.model.rest.unit.custom;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import tech.ebp.oqm.core.api.model.units.CustomUnitEntry;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.units.UnitCategory;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.model.units.ValidUnitDimension;

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
	
	@ParameterizedTest
	@MethodSource("getObjects")
	public void testResultingUnitSerializable(NewCustomUnitRequest cur) throws JsonProcessingException {
		CustomUnitEntry cue = cur.toCustomUnitEntry(0L);
		
		UnitUtils.registerAllUnits(cue);
		
		OBJECT_MAPPER.writeValueAsString(cue);
	}
	
	@AfterEach
	public void cleanupUnits() {
		UnitUtils.reInitUnitCollections();
	}
}