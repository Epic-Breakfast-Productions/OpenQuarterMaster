package tech.ebp.oqm.core.api.model.object.storage.items.pricing;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;

import javax.money.Monetary;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class TotalPricingTest extends ObjectSerializationTest<TotalPricing> {
	
	protected TotalPricingTest() {
		super(TotalPricing.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				TotalPricing.builder()
					.label(FAKER.name().name())
					.totalPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
					.build()
			)
		);
	}
	
	@Test
	public void testAddPrice(){
		//TODO
	}
}