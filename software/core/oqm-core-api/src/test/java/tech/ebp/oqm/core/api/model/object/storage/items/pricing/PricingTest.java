package tech.ebp.oqm.core.api.model.object.storage.items.pricing;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;

import javax.money.Monetary;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tech.ebp.oqm.core.api.model.testUtils.BasicTest.FAKER;

@Slf4j
public class PricingTest {
	
	@Test
	public void testPricingFormat() {
		assertEquals(
			"$1.00",
			Pricing.format(
				Monetary.getDefaultAmountFactory()
					.setCurrency("USD").setNumber(1).create()
			)
		);
	}
}