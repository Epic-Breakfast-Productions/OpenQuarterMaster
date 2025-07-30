package tech.ebp.oqm.core.api.testResources.upgrader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.testResources.data.TestVersionableObject;

public class TestVersion4Bumper extends ObjectSchemaVersionBumper<TestVersionableObject> {
	
	public TestVersion4Bumper() {
		super(4);
	}
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		oldObj.put("baz", 4);
		
		return SingleUpgradeResult.builder()
				   .upgradedObject(oldObj)
				   .build();
	}
}
