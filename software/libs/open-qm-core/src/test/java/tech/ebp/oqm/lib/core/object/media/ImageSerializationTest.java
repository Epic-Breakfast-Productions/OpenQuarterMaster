package tech.ebp.oqm.lib.core.object.media;

import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

@Slf4j
class ImageSerializationTest extends ObjectSerializationTest<Image> {
	
	protected ImageSerializationTest() {
		super(Image.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new Image(
				BasicTest.FAKER.job().title(),
				BasicTest.FAKER.lorem().paragraph(),
				"png",
				new String(Base64.getEncoder().encode(BasicTest.FAKER.lorem().paragraph().getBytes(StandardCharsets.UTF_8)))
			))
		);
	}
	
}