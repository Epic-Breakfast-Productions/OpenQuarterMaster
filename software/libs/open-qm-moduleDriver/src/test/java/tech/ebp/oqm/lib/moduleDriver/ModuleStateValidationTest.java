package tech.ebp.oqm.lib.moduleDriver;

import tech.ebp.oqm.lib.moduleDriver.testUtils.ObjectValidationTest;
import org.junit.jupiter.params.provider.Arguments;

import java.util.HashMap;
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