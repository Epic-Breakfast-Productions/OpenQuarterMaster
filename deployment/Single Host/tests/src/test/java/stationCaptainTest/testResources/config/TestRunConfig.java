package stationCaptainTest.testResources.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stationCaptainTest.testResources.config.snhSetup.SnhSetupConfig;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunConfig {
	private SnhSetupConfig setupConfig;
}
