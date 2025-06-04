package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.exception.VersionBumperListIncontiguousException;
import tech.ebp.oqm.core.api.model.object.upgrade.ObjectUpgradeResult;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;
import tech.ebp.oqm.core.api.testResources.data.TestVersionableObject;
import tech.ebp.oqm.core.api.testResources.upgrader.TestVersion2Bumper;
import tech.ebp.oqm.core.api.testResources.upgrader.TestVersion3Bumper;
import tech.ebp.oqm.core.api.testResources.upgrader.TestVersion4Bumper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ObjectSchemaUpgraderTest extends BasicTest {
	
	public static class TestObjectSchemaUpgrader extends ObjectSchemaUpgrader<TestVersionableObject> {
		
		public TestObjectSchemaUpgrader(ObjectSchemaVersionBumper<TestVersionableObject>... versionBumpers)
			throws VersionBumperListIncontiguousException {
			super(TestVersionableObject.class, versionBumpers);
		}
		
		public TestObjectSchemaUpgrader() {
			this(
				new TestVersion2Bumper(),
				new TestVersion3Bumper(),
				new TestVersion4Bumper()
				);
		}
	}
	
	@Test
	public void testUpgradeJsonFrom1(){
		TestObjectSchemaUpgrader upgrader = new TestObjectSchemaUpgrader();
		
		ObjectNode oldObj = OBJECT_MAPPER.createObjectNode();
		oldObj.put(ObjectSchemaVersionBumper.SCHEMA_VERSION_FIELD, 1);
		oldObj.put("name", "test");
		
		ObjectUpgradeResult<TestVersionableObject> result = upgrader.upgrade(oldObj);
		
		log.info("Upgrade result: {}", result);
		
		assertTrue(result.wasUpgraded());
		assertEquals(1, result.getOldVersion());
		assertNotNull(result.getTimeTaken());
		assertEquals(3, result.getNumVersionsBumped());
	}
	
	@Test
	public void testUpgradeJsonFrom2(){
		TestObjectSchemaUpgrader upgrader = new TestObjectSchemaUpgrader();
		
		ObjectNode oldObj = OBJECT_MAPPER.createObjectNode();
		oldObj.put(ObjectSchemaVersionBumper.SCHEMA_VERSION_FIELD, 2);
		oldObj.put("name", "test");
		
		ObjectUpgradeResult<TestVersionableObject> result = upgrader.upgrade(oldObj);
		
		log.info("Upgrade result: {}", result);
		
		assertTrue(result.wasUpgraded());
		assertEquals(2, result.getOldVersion());
		assertNotNull(result.getTimeTaken());
		assertEquals(2, result.getNumVersionsBumped());
	}
	
	@Test
	public void testUpgradeDocument() throws JsonProcessingException {
		TestObjectSchemaUpgrader upgrader = new TestObjectSchemaUpgrader();
		
		ObjectNode oldObjJson = OBJECT_MAPPER.createObjectNode();
		oldObjJson.put(ObjectSchemaVersionBumper.SCHEMA_VERSION_FIELD, 1);
		oldObjJson.put("name", "test");
		
		Document oldObj = Document.parse(oldObjJson.toString());
		
		ObjectUpgradeResult<TestVersionableObject> result = upgrader.upgrade(oldObj);
		
		log.info("Upgrade result: {}", result);
		
		assertTrue(result.wasUpgraded());
		assertEquals(1, result.getOldVersion());
		assertNotNull(result.getTimeTaken());
		assertEquals(3, result.getNumVersionsBumped());
	}
	
	@Test
	public void testConstructorIncontiguousNoStart() {
		assertThrows(
			VersionBumperListIncontiguousException.class,
			()-> new TestObjectSchemaUpgrader(
				new TestVersion3Bumper(),
				new TestVersion4Bumper()
			)
		);
	}
	
	@Test
	public void testConstructorIncontiguousNoMid() {
		assertThrows(
			VersionBumperListIncontiguousException.class,
			()-> new TestObjectSchemaUpgrader(
				new TestVersion2Bumper(),
				new TestVersion4Bumper()
			)
		);
	}
}
