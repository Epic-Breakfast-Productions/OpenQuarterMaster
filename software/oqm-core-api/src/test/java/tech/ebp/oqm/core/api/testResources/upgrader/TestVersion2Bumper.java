package tech.ebp.oqm.core.api.testResources.upgrader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.testResources.data.TestVersionableObject;

public class TestVersion2Bumper extends ObjectSchemaVersionBumper<TestVersionableObject> {
	
	public TestVersion2Bumper() {
		super(2);
	}
	
	@Override
	protected ObjectNode bumpObjectSchema(ObjectNode oldObj) {
		oldObj.put("foo", 2);
		oldObj.put("bar", "2");
		
		return oldObj;
	}
}
