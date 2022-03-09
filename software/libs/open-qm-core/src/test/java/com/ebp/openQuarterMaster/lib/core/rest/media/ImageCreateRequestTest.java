package com.ebp.openQuarterMaster.lib.core.rest.media;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.bson.internal.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TODO:: make other validator tests use this real validator as opposed to how they currently operate
 */
@Slf4j
class ImageCreateRequestTest {
	
	private Validator validator;
	
	@BeforeEach
	public void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}
	
	public static Stream<Arguments> validObjects() {
		return Stream.of(
			Arguments.of(new ImageCreateRequest(
				BasicTest.FAKER.animal().name(),
				BasicTest.FAKER.lorem().sentence(),
				Base64.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8)),
				new ArrayList<>(),
				new HashMap<>()
			)),
			Arguments.of(new ImageCreateRequest(
				BasicTest.FAKER.animal().name(),
				BasicTest.FAKER.lorem().sentence(),
				"data:image/png;base64," + Base64.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8)),
				new ArrayList<>(),
				new HashMap<>()
			))
		);
	}
	
	public static Stream<Arguments> invalidObjects() throws JsonProcessingException {
		return Stream.of(
			Arguments.of(new ImageCreateRequest(
				BasicTest.FAKER.animal().name(),
				BasicTest.FAKER.lorem().sentence(),
				"foo\\bard" + Base64.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8)),
				new ArrayList<>(),
				new HashMap<>()
			)),
			Arguments.of(Utils.OBJECT_MAPPER.readValue(
				"{" +
				"   \"title\": \"\"," +
				"   \"description\": \"" + BasicTest.FAKER.lorem().sentence() + "\"," +
				"   \"imageData\": \"" + Base64.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8)) + "\"," +
				"   \"keywords\": []," +
				"   \"attributes\": {}" +
				"}",
				ImageCreateRequest.class
			)),
			Arguments.of(Utils.OBJECT_MAPPER.readValue(
				"{" +
				"   \"title\": \" \"," +
				"   \"description\": \"" + BasicTest.FAKER.lorem().sentence() + "\"," +
				"   \"imageData\": \"" + Base64.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8)) + "\"," +
				"   \"keywords\": []," +
				"   \"attributes\": {}" +
				"}",
				ImageCreateRequest.class
			))
		);
	}
	
	
	@ParameterizedTest
	@MethodSource("validObjects")
	public void testValid(ImageCreateRequest icr) {
		Set<ConstraintViolation<ImageCreateRequest>> violations = validator.validate(icr);
		
		log.info("Violations: {}", violations);
		assertTrue(violations.isEmpty());
	}
	
	@ParameterizedTest
	@MethodSource("invalidObjects")
	public void testInvalid(ImageCreateRequest icr) {
		Set<ConstraintViolation<ImageCreateRequest>> violations = validator.validate(icr);
		
		log.info("Violations: {}", violations);
		assertFalse(violations.isEmpty());
	}
}