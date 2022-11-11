package tech.ebp.oqm.lib.core.object.externalService;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;

import java.util.stream.Stream;

@Slf4j
class GeneralServiceSerializationTest extends ObjectSerializationTest<GeneralService> {
	
	protected GeneralServiceSerializationTest() {
		super(GeneralService.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				new GeneralService()
					.setName(FAKER.name().name())
					.setDeveloperName(FAKER.name().name())
					.setDeveloperEmail(FAKER.internet().emailAddress())
			)
		);
	}
	
}