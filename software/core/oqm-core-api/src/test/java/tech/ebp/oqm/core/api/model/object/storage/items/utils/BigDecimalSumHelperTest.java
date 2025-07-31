package tech.ebp.oqm.core.api.model.object.storage.items.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BigDecimalSumHelperTest extends BasicTest {
	
	public static Stream<Arguments> getTotalArguments() {
		return Stream.of(
			Arguments.of(
				List.of(),
				new BigDecimal("0.0")
			),
			Arguments.of(
				List.of(new BigDecimal(1.0)),
				new BigDecimal("1.0")
			),
			Arguments.of(
				List.of(
					new BigDecimal(1.0),
					new BigDecimal(1.0)
				),
				new BigDecimal("2.0")
			)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getTotalArguments")
	public void testTotalTest(List<BigDecimal> list, BigDecimal quantityExpected) {
		BigDecimalSumHelper helper = new BigDecimalSumHelper();
		helper.addAll(list);
		assertEquals(
			quantityExpected,
			helper.getTotal()
		);
	}
	
}