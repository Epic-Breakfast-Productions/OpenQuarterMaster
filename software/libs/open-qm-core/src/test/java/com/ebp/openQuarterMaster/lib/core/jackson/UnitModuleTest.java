package com.ebp.openQuarterMaster.lib.core.jackson;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.measure.Unit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class UnitModuleTest extends BasicTest {
	
	private static Stream<Arguments> unitsAsArgs() {
		return UnitUtils.ALLOWED_UNITS.stream().map(Arguments::of);
	}
	
	private final UnitModule module = new UnitModule();
	
	@ParameterizedTest
	@MethodSource("unitsAsArgs")
	public void testModuleSerializationValid(Unit<?> unit) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator gen = OBJECT_MAPPER.createGenerator(os);
		
		module.getSerializer().serialize(unit, gen, null);
		
		log.info("Serialized Unit: {}", os);
		
		ObjectNode result = (ObjectNode) OBJECT_MAPPER.readTree(new ByteArrayInputStream(os.toByteArray()));
		
		assertEquals(
			UnitUtils.stringFromUnit(unit),
			result.get("string").asText()
		);
		assertEquals(
			unit.getName(),
			result.get("name").asText()
		);
		assertEquals(
			unit.getSymbol(),
			result.get("symbol").asText()
		);
	}
	
	@ParameterizedTest
	@MethodSource("unitsAsArgs")
	public void testModuleDeserializationValid(Unit<?> unit) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator gen = OBJECT_MAPPER.createGenerator(os);
		
		module.getSerializer().serialize(unit, gen, null);
		
		log.info("Serialized Unit: {}", os);
		
		Unit<?> result = module.getDeserializer().deserialize(
			OBJECT_MAPPER.createParser(new ByteArrayInputStream(os.toByteArray())),
			null
		);
		
		assertEquals(unit, result);
	}
	
	//TODO:: test serialization with invalid units
	
}