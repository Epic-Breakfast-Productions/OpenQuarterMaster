package stationCaptainTest.testResources.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunConfig {
	@Builder.Default
	private InstanceConnectionConfig instance = new InstanceConnectionConfig();
	@Builder.Default
	private boolean cleanupAfter = Boolean.parseBoolean(System.getProperty("testconfig.cleanupAfter", "true"));
}
