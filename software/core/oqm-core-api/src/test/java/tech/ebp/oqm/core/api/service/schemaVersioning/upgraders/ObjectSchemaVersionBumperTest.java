package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;
import tech.ebp.oqm.core.api.testResources.data.TestVersionableObject;
import tech.ebp.oqm.core.api.testResources.upgrader.TestVersion2Bumper;
import tech.ebp.oqm.core.api.testResources.upgrader.TestVersion3Bumper;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ObjectSchemaVersionBumperTest extends BasicTest {
	
	private final TestVersion2Bumper bumper2 = new TestVersion2Bumper();
	private final TestVersion3Bumper bumper3 = new TestVersion3Bumper();
	
	@Test
	public void testCompare() {
		assertEquals(-1, bumper2.compareTo(bumper3));
		assertEquals(1, bumper3.compareTo(bumper2));
		assertEquals(0, bumper2.compareTo(bumper2));
		
		TreeSet<ObjectSchemaVersionBumper<TestVersionableObject>> set = new TreeSet<>();
		set.add(bumper3);
		set.add(bumper2);
		
		assertEquals(bumper2, set.first());
		assertEquals(bumper3, set.last());
	}
	
	@Test
	public void testBumpObject() {
		ObjectNode oldObj = OBJECT_MAPPER.createObjectNode();
		oldObj.put(ObjectSchemaVersionBumper.SCHEMA_VERSION_FIELD, 1);
		oldObj.put("name", "test");
		
		SingleUpgradeResult result = this.bumper2.bumpObject(oldObj);
		ObjectNode oldObj2 = result.getUpgradedObject();
		assertEquals(2, oldObj2.get(ObjectSchemaVersionBumper.SCHEMA_VERSION_FIELD).asInt());
		assertTrue(result.hasCreatedObjects());
		
		result = this.bumper3.bumpObject(oldObj2);
		ObjectNode oldObj3 = result.getUpgradedObject();
		assertEquals(3, oldObj3.get(ObjectSchemaVersionBumper.SCHEMA_VERSION_FIELD).asInt());
		assertFalse(result.hasCreatedObjects());
	}
	
	@Test
	public void testBumpNoVersion() {
		ObjectNode oldObj = OBJECT_MAPPER.createObjectNode();
		oldObj.put("name", "test");
		
		assertThrows(
			IllegalArgumentException.class,
			()->this.bumper2.bumpObject(oldObj)
		);
	}
	
	@Test
	public void testBumpSameVersion() {
		ObjectNode oldObj = OBJECT_MAPPER.createObjectNode();
		oldObj.put(ObjectSchemaVersionBumper.SCHEMA_VERSION_FIELD, 2);
		oldObj.put("name", "test");
		
		assertThrows(
			IllegalArgumentException.class,
			()->this.bumper2.bumpObject(oldObj)
		);
	}
}
