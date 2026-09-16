package tech.ebp.oqm.plugin.mssController.testResources.profiles;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;


public class NoModulesTestProfile implements QuarkusTestProfile {
	@Override
	public Map<String, String> getConfigOverrides() {
		return Map.of(
			"quarkus.mssControllerDev.devservices.modules[0].type", "SERIAL"
		);
	}
}
