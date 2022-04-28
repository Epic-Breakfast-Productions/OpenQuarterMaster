package com.ebp.openQuarterMaster.lib.driver;

import com.ebp.openQuarterMaster.lib.driver.testUtils.ObjectValidationTest;
import org.junit.jupiter.params.provider.Arguments;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

class ModuleStateValidationTest extends ObjectValidationTest<ModuleState> {
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(new ModuleState())
		);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new ModuleState() {{
					this.getLightSettings().add(null);
				}},
				new HashMap<>() {{
					put("lightSettings\\[0].<list element>", "must not be null");
				}}
			)
		);
	}
	
}