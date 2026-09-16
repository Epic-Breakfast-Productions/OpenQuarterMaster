package tech.ebp.oqm.core.api.model.object.storage.items.pricing;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import tech.ebp.oqm.core.api.model.units.UnitUtils;

import javax.measure.Unit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tech.ebp.oqm.core.api.model.testUtils.BasicTest.FAKER;

@Slf4j
public class PricingTest {

	public static Stream<Arguments> pricingArgs() {
		return Stream.of(
			Arguments.of("$1.00", Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create()),
			Arguments.of("$1,000.00", Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1000).create())
		);
	}

	@ParameterizedTest
	@MethodSource("pricingArgs")
	public void testPricingFormat(String expected, MonetaryAmount pricing) {
		assertEquals(
			expected,
			Pricing.format(
				pricing
			)
		);
	}
}
