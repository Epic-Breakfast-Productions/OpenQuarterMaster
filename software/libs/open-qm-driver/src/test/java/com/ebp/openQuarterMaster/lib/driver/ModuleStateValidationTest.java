package com.ebp.openQuarterMaster.lib.driver;

import com.ebp.openQuarterMaster.lib.driver.testUtils.ObjectValidationTest;
import org.junit.jupiter.params.provider.Arguments;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

class ModuleStateValidationTest extends ObjectValidationTest<ModuleState> {
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(new ModuleState().setSerialNo(UUID.randomUUID().toString()))
		);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new ModuleState(),
				new HashMap<>() {{
					put("serialNo", "must not be null");
				}}
			)
		);
	}
	
}