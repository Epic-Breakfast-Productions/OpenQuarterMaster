package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class UpgradingUtilsTest extends BasicTest {
	
	@Test
	public void normalizeObjectIdDiffFieldTest(){
		ObjectNode input = OBJECT_MAPPER.createObjectNode();
		input.putObject("_id").put("$oid", "newId");
		
		ObjectNode expected = OBJECT_MAPPER.createObjectNode();
		expected.put("id", "newId");
		
		UpgradingUtils.normalizeObjectId(input, "_id", "id");
		assertEquals(expected, input);
	}
	
	@Test
	public void normalizeObjectIdSameFieldTest(){
		ObjectNode input = OBJECT_MAPPER.createObjectNode();
		input.putObject("id").put("$oid", "newId");
		
		ObjectNode expected = OBJECT_MAPPER.createObjectNode();
		expected.put("id", "newId");
		
		UpgradingUtils.normalizeObjectId(input, "id", "id");
		assertEquals(expected, input);
	}
	
	@Test
	public void normalizeObjectIdAlreadyNormalizedTest(){
		ObjectNode input = OBJECT_MAPPER.createObjectNode();
		input.put("id", "newId");
		
		ObjectNode expected = OBJECT_MAPPER.createObjectNode();
		expected.put("id", "newId");
		
		UpgradingUtils.normalizeObjectId(input, "_id", "id");
		assertEquals(expected, input);
	}
	
	@Test
	public void normalizeObjectIdAlreadyNormalizedSameFieldTest(){
		ObjectNode input = OBJECT_MAPPER.createObjectNode();
		input.put("id", "newId");
		
		ObjectNode expected = OBJECT_MAPPER.createObjectNode();
		expected.put("id", "newId");
		
		UpgradingUtils.normalizeObjectId(input, "id", "id");
		assertEquals(expected, input);
	}
	
	@Test
	public void normalizeObjectIdNullValueTest(){
		ObjectNode input = OBJECT_MAPPER.createObjectNode();
		input.putObject("_id").putNull("$oid");
		
		ObjectNode expected = OBJECT_MAPPER.createObjectNode();
		expected.putNull("id");
		
		UpgradingUtils.normalizeObjectId(input, "_id", "id");
		assertEquals(expected, input);
	}
	
	@Test
	public void normalizeObjectIdNullValueSameFieldTest(){
		ObjectNode input = OBJECT_MAPPER.createObjectNode();
		input.putObject("id").putNull("$oid");
		
		ObjectNode expected = OBJECT_MAPPER.createObjectNode();
		expected.putNull("id");
		
		UpgradingUtils.normalizeObjectId(input, "id", "id");
		assertEquals(expected, input);
	}
	
}
