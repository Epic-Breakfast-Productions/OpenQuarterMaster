package com.ebp.openQuarterMaster.lib.core.test;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Deprecated
class TestSuperTest {
	
	public static Stream<Arguments> getObjs() {
		return Stream.of(
			Arguments.of(new TestOne("something")),
			Arguments.of(new TestTwo(3))
		);
	}
	
	@ParameterizedTest
	@MethodSource("getObjs")
	public void serializationTest(TestSuper<?> obj) throws JsonProcessingException {
		log.info("Object: {}", obj);
		log.info("Class: {}", obj.getClass());
		log.info("Original obj type: {}", obj.getType());
		String json = Utils.OBJECT_MAPPER.writeValueAsString(obj);
		log.info("Object json: {}", json);
		
		TestSuper<?> deSerialized = Utils.OBJECT_MAPPER.readValue(json, TestSuper.class);
		log.info("Deserialized object: {}", obj);
		log.info("Deserialized class: {}", obj.getClass());
		log.info("Deserialized obj type: {}", deSerialized.getType());
		assertEquals(obj, deSerialized);
	}
	
}