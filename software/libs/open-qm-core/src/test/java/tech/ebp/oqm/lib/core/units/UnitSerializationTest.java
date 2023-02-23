package tech.ebp.oqm.lib.core.units;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.lib.core.object.ObjectUtils;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import tech.uom.lib.jackson.UnitJacksonModule;

import javax.measure.Unit;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class UnitSerializationTest extends BasicTest {
	
	
	private static Stream<Arguments> unitsAsArgs() {
		return UnitUtils.UNIT_LIST.stream().map(Arguments::of);
	}
	
	@ParameterizedTest
	@MethodSource("unitsAsArgs")
	public void playground(Unit<?> unit) throws JsonProcessingException {
		
		ObjectMapper mapper = ObjectUtils.OBJECT_MAPPER;// new ObjectMapper();
		
		mapper = mapper.registerModules(
			new UnitJacksonModule()
		);
		
		
		String unitString = mapper.writeValueAsString(unit);
		
		log.info("Unit String: {}", unitString);
		
		Unit<?> unitOut = mapper.readValue(unitString, Unit.class);
		
		assertEquals(unit, unitOut);
	}
	
}