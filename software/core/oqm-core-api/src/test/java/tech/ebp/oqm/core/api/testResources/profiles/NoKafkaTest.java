package tech.ebp.oqm.core.api.testResources.profiles;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;


public class NoKafkaTest implements QuarkusTestProfile {
	@Override
	public Map<String, String> getConfigOverrides() {
		return Map.of(
			"mp.messaging.outgoing.events-outgoing.enabled", "false"
		);
	}
}
