package tech.ebp.oqm.lib.core.object.externalService;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.lib.core.object.externalService.plugin.PluginService;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;

import java.util.stream.Stream;

@Slf4j
class PluginServiceSerializationTest extends ObjectSerializationTest<PluginService> {
	
	protected PluginServiceSerializationTest() {
		super(PluginService.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				new PluginService()
					.setName(FAKER.name().name())
			)
		);
	}
	
}