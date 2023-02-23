package tech.ebp.oqm.lib.core.object.interactingEntity.externalService;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.lib.core.object.interactingEntity.externalService.plugin.PluginService;
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
					.setDeveloperName(FAKER.name().name())
					.setDeveloperEmail(FAKER.internet().emailAddress())
					.setId(ObjectId.get())
			)
		);
	}
	
}