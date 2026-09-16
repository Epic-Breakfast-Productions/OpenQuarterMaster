package tech.ebp.oqm.plugin.mssController.testModuleServer.testProfiles;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;


public class SerialPortTestProfile implements QuarkusTestProfile {
	@Override
	public Map<String, String> getConfigOverrides() {
		return Map.of(
			"moduleConfig.type", "SERIAL"
		);
	}
}
