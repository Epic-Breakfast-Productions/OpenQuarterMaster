package stationCaptainTest.testResources.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunConfig {
	@Builder.Default
	private InstanceConnectionConfig instance = new InstanceConnectionConfig();
	@Builder.Default
	private boolean cleanupAfter = Boolean.parseBoolean(System.getProperty("testconfig.cleanupAfter", "true"));
	@Builder.Default
	private Duration testSpacerWait = Duration.of(10, ChronoUnit.SECONDS);
	
}
