package tech.ebp.oqm.core.api.testResources.upgrader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.testResources.data.TestVersionableObject;

import java.util.HashMap;
import java.util.List;

import static tech.ebp.oqm.core.api.model.object.ObjectUtils.OBJECT_MAPPER;

public class TestVersion2Bumper extends ObjectSchemaVersionBumper<TestVersionableObject> {
	
	public TestVersion2Bumper() {
		super(2);
	}
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		oldObj.put("foo", 2);
		oldObj.put("bar", "2");
		
		return SingleUpgradeResult.builder()
				   .upgradedObject(oldObj)
				   .createdObjects(
					  new UpgradeCreatedObjectsResults() {{
						  ObjectNode newObj = OBJECT_MAPPER.createObjectNode();
						  newObj.put(ObjectSchemaVersionBumper.SCHEMA_VERSION_FIELD, 1);
						  newObj.put("name", "test");
						  put(TestVersionableObject.class, List.of(newObj));
					  }}
				   )
				   .build();
	}
}
