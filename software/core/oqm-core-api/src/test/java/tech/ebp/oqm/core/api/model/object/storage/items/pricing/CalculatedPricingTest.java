package tech.ebp.oqm.core.api.model.object.storage.items.pricing;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.unit.CalculatedPricePerUnit;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;

import javax.money.Monetary;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class CalculatedPricingTest extends ObjectSerializationTest<CalculatedPricing> {
	
	protected CalculatedPricingTest() {
		super(CalculatedPricing.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				CalculatedPricing.builder()
					.label(FAKER.name().name())
					.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
					.build()
			)
		);
	}
	
	@Test
	public void testTotalJustFlat(){
		assertEquals(
			Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create(),
			CalculatedPricing.builder()
				.label(FAKER.name().name())
				.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
				.build().getTotalPrice()
		);
	}
	
	@Test
	public void testTotalWithUnit(){
		assertEquals(
			Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(2).create(),
			CalculatedPricing.builder()
				.label(FAKER.name().name())
				.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
				.perUnitPrice(
					CalculatedPricePerUnit.builder()
						.unit(OqmProvidedUnits.UNIT)
						.price(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
						.totalPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
						.build()
				)
				.build().getTotalPrice()
		);
	}
}