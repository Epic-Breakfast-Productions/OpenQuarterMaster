package tech.ebp.oqm.core.api.testResources.upgrader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.testResources.data.TestVersionableObject;

public class TestVersion3Bumper extends ObjectSchemaVersionBumper<TestVersionableObject> {
	
	public TestVersion3Bumper() {
		super(3);
	}
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		Integer bar = null;
		if(oldObj.has("bar")){
			bar = oldObj.get("bar").asInt();
		}
		oldObj.put("bar", bar);
		
		return SingleUpgradeResult.builder()
				   .upgradedObject(oldObj)
				   .build();
	}
}
