package tech.ebp.oqm.lib.core.object.storage.items.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.AmountOfSubstance;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class QuantitySumHelperTest extends BasicTest {
	
	public static Stream<Arguments> getTotalArguments() {
		return Stream.of(
			Arguments.of(
				List.of(),
				UnitUtils.UNIT,
				Quantities.getQuantity(0.0, UnitUtils.UNIT)
			),
			Arguments.of(
				List.of(
					Quantities.getQuantity(0.0, UnitUtils.UNIT)
				),
				UnitUtils.UNIT,
				Quantities.getQuantity(0.0, UnitUtils.UNIT)
			),
			Arguments.of(
				List.of(
					Quantities.getQuantity(1.0, UnitUtils.UNIT)
				),
				UnitUtils.UNIT,
				Quantities.getQuantity(1.0, UnitUtils.UNIT)
			),
			//			Arguments.of( //TODO:: https://github.com/unitsofmeasurement/indriya/issues/384
			//				List.of(
			//					Quantities.getQuantity(1.1, UnitUtils.UNIT)
			//				),
			//				UnitUtils.UNIT,
			//				Quantities.getQuantity(new BigDecimal("1.1"), UnitUtils.UNIT)
			//			),
			Arguments.of(
				List.of(
					Quantities.getQuantity(1.1, UnitUtils.ALLOWED_UNITS.get(2))
				),
				UnitUtils.ALLOWED_UNITS.get(2),
				Quantities.getQuantity(new BigDecimal("1.1"), UnitUtils.ALLOWED_UNITS.get(2))
			)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getTotalArguments")
	public void testTotalTest(List<Quantity<?>> list, Unit<?> unit, Quantity<?> quantityExpected) {
		QuantitySumHelper helper = new QuantitySumHelper(unit);
		helper.addAll(list);
		log.info("total: {}", helper.getTotal().getValue());
		assertEquals(
			quantityExpected.getValue(),
			helper.getTotal().getValue()
		);
	}
	
	@Disabled()// TODO:: https://github.com/unitsofmeasurement/indriya/issues/384
	@Test
	public void playground() {
		Unit<AmountOfSubstance> unit = Units.MOLE;
		
		Quantity<?> result = Quantities.getQuantity(0.0, unit).add(Quantities.getQuantity(1.1, Units.MOLE));
		
		assertEquals(
			Quantities.getQuantity(1.1, unit),
			result
		);
	}
	
	@Test
	public void playground2() {
		Unit<AmountOfSubstance> unit = Units.MOLE;
		
		assertEquals(
			Quantities.getQuantity(1.1, unit),
			Quantities.getQuantity(1.1, unit)
		);
	}
	
	@Test
	public void playground3() {
		Unit<AmountOfSubstance> unit = Units.MOLE;
		
		Quantity<?> result = Quantities.getQuantity(0, unit).add(Quantities.getQuantity(1, Units.MOLE));
		
		assertEquals(
			Quantities.getQuantity(1, unit),
			result
		);
	}
	
}