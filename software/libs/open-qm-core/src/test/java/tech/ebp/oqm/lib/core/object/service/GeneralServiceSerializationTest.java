package tech.ebp.oqm.lib.core.object.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.lib.core.object.media.Image;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
			)
		);
	}
	
}