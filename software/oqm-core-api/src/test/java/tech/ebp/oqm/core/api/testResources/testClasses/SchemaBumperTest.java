package tech.ebp.oqm.core.api.testResources.testClasses;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.bumpers.InvItemBumper2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
public abstract class SchemaBumperTest<T extends ObjectSchemaVersionBumper<?>> extends BasicTest {
	
	private TestInfo testInfo;
	private final T bumper;
	
	protected SchemaBumperTest(T bumper) {
		this.bumper = bumper;
	}
	
	@BeforeEach
	public void stateThresholds(TestInfo testInfo) {
		this.testInfo = testInfo;
	}
	
	@ParameterizedTest
	@MethodSource("getObjects")
	public void testSchemaBump(
		ObjectNode oldObj,
		ObjectNode expectedNewObj,
		Map<Class<?>, ObjectNode> expectedCreatedObjects
	) throws IOException {
		log.info("Testing Schema Bumper: {}", this.bumper);
		
		SingleUpgradeResult result = this.bumper.bumpObject(oldObj);
		
		log.info("Result: {}", result);
		
		assertEquals(
			expectedNewObj,
			result.getUpgradedObject()
		);
		assertEquals(
			expectedCreatedObjects,
			new HashMap<>(result.getCreatedObjects())
		);
	}
	
}
