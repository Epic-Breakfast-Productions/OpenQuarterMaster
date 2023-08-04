package tech.ebp.oqm.baseStation.model.objectUpgrade.testUtil;

import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.baseStation.model.objectUpgrade.ObjectVersionBumper;

public class TestObjectVersionBumper extends ObjectVersionBumper<TestVersionable> {
	
	public TestObjectVersionBumper(int bumperTo) {
		super(bumperTo);
	}
	
	@Override
	public JsonNode bumpObject(JsonNode oldObj) {
		return null;
	}
	
}
