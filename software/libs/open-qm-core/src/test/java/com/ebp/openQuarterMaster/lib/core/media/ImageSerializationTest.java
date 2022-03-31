package com.ebp.openQuarterMaster.lib.core.media;

import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectSerializationTest;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectValidationTest;
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