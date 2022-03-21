package com.ebp.openQuarterMaster.lib.core.testUtils;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public abstract class ObjectSerializationTest<T> extends BasicTest {
	
	private final Class<T> clazz;
	
	protected ObjectSerializationTest(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@ParameterizedTest
	@MethodSource("getObjects")
	public void testSerialization(T object, boolean logJson) throws JsonProcessingException {
		StopWatch sw = StopWatch.createStarted();
		String json = OBJECT_MAPPER.writeValueAsString(object);
		sw.stop();
		log.info("Serialized object in {}", sw);
		
		if (logJson) {
			log.info("json: {}", json);
		}
		
		sw = StopWatch.createStarted();
		T objectBack = OBJECT_MAPPER.readValue(json, clazz);
		sw.stop();
		log.info("Deserialized object in {}", sw);
		
		assertEquals(object, objectBack, "Deserialized object was not equal to original.");
	}
	
}
