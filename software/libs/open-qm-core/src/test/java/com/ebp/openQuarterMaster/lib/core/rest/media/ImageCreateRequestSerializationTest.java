package com.ebp.openQuarterMaster.lib.core.rest.media;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectSerializationTest;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectValidationTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * TODO:: make other validator tests use this real validator as opposed to how they currently operate
 */
@Slf4j
class ImageCreateRequestSerializationTest extends ObjectSerializationTest<ImageCreateRequest> {
	
	private static final Base64.Encoder ENCODER = Base64.getEncoder();
	
	protected ImageCreateRequestSerializationTest() {
		super(ImageCreateRequest.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new ImageCreateRequest(
				BasicTest.FAKER.animal().name(),
				BasicTest.FAKER.lorem().sentence(),
				new String(ENCODER.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8))),
				new ArrayList<>(),
				new HashMap<>()
			)),
			Arguments.of(new ImageCreateRequest(
				BasicTest.FAKER.animal().name(),
				BasicTest.FAKER.lorem().sentence(),
				"data:image/png;base64," + new String(ENCODER.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8))),
				new ArrayList<>(),
				new HashMap<>()
			))
		);
	}
}