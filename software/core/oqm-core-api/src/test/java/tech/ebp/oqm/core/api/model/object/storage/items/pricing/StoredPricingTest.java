package tech.ebp.oqm.core.api.model.object.storage.items.pricing;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;

import javax.money.Monetary;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class StoredPricingTest extends ObjectSerializationTest<StoredPricing> {
	
	protected StoredPricingTest() {
		super(StoredPricing.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				StoredPricing.builder()
					.label(FAKER.name().name())
					.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
					.build()
			)
		);
	}
	
	@Test
	public void testCalculateJustFlat(){
		String label = FAKER.name().name();
		assertEquals(
			CalculatedPricing.builder()
				.label(label)
				.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
				.build(),
			StoredPricing.builder()
				.label(label)
				.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
				.build().calculatePrice(null)
		);
	}
}