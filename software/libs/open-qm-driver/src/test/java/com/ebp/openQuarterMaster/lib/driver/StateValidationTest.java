package com.ebp.openQuarterMaster.lib.driver;

import com.ebp.openQuarterMaster.lib.driver.testUtils.ObjectValidationTest;
import org.junit.jupiter.params.provider.Arguments;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

class StateValidationTest extends ObjectValidationTest<State> {
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(new State().setSerialNo(UUID.randomUUID().toString()))
		);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new State(),
				new HashMap<>() {{
					put("serialNo", "must not be null");
				}}
			)
		);
	}
	
}