package com.ebp.openQuarterMaster.lib.core.object.media;

import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectValidationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

@Slf4j
class ImageValidationTest extends ObjectValidationTest<Image> {
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(new Image(
				BasicTest.FAKER.job().title(),
				BasicTest.FAKER.lorem().paragraph(),
				"png",
				new String(Base64.getEncoder().encode(BasicTest.FAKER.lorem().paragraph().getBytes(StandardCharsets.UTF_8)))
			))
		);
	}
	
	//TODO:: fill out error message validation
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new Image(
					BasicTest.FAKER.job().title(),
					BasicTest.FAKER.lorem().paragraph(),
					"",
					new String(Base64.getEncoder().encode(BasicTest.FAKER.lorem().paragraph().getBytes(StandardCharsets.UTF_8)))
				),
				null
			),
			Arguments.of(
				new Image(
					BasicTest.FAKER.job().title(),
					BasicTest.FAKER.lorem().paragraph(),
					BasicTest.FAKER.random().hex(),
					new String(Base64.getEncoder().encode(BasicTest.FAKER.lorem().paragraph().getBytes(StandardCharsets.UTF_8)))
				),
				null
			),
			Arguments.of(
				new Image(
					"",
					BasicTest.FAKER.lorem().paragraph(),
					"png",
					new String(Base64.getEncoder().encode(BasicTest.FAKER.lorem().paragraph().getBytes(StandardCharsets.UTF_8)))
				),
				null
			),
			Arguments.of(
				new Image(
					" ",
					BasicTest.FAKER.lorem().paragraph(),
					"png",
					new String(Base64.getEncoder().encode(BasicTest.FAKER.lorem().paragraph().getBytes(StandardCharsets.UTF_8)))
				),
				null
			),
			Arguments.of(
				new Image(
					BasicTest.FAKER.job().title(),
					BasicTest.FAKER.lorem().characters(501),
					"png",
					new String(Base64.getEncoder().encode(BasicTest.FAKER.lorem().paragraph().getBytes(StandardCharsets.UTF_8)))
				),
				null
			),
			Arguments.of(
				new Image(
					BasicTest.FAKER.job().title(),
					BasicTest.FAKER.lorem().paragraph(),
					"png",
					""
				),
				null
			),
			Arguments.of(
				new Image(
					BasicTest.FAKER.job().title(),
					BasicTest.FAKER.lorem().paragraph(),
					"png",
					BasicTest.FAKER.lorem().paragraph()
				),
				null
			)
		);
	}
}