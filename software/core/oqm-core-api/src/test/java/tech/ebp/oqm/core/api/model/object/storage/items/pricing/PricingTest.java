package tech.ebp.oqm.core.api.model.object.storage.items.pricing;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import tech.ebp.oqm.core.api.testResources.testClasses.WebServerTest;

import javax.money.Monetary;
import javax.money.MonetaryAmount;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class PricingTest extends ObjectSerializationTest<Pricing> {
	
	protected PricingTest() {
		super(Pricing.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				Pricing.builder()
					.label(FAKER.name().name())
					.price(Monetary.getDefaultAmountFactory()
							   .setCurrency("USD").setNumber(1).create())
					.build()
			)
		);
	}
	
	@Test
	public void testPricingFormat(){
		assertEquals(
			"$1.00",
			Pricing.builder()
				.label(FAKER.name().name())
				.price(Monetary.getDefaultAmountFactory()
						   .setCurrency("USD").setNumber(1).create())
				.build()
				.getPriceString()
		);
	}
}