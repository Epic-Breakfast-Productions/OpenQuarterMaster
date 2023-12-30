package stationCaptainTest.testResources.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stationCaptainTest.testResources.config.snhSetup.ContainerSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.installType.RepoInstallTypeConfig;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunConfig {
	private SnhSetupConfig setupConfig = new ContainerSnhSetupConfig();
	private boolean cleanupAfter = Boolean.parseBoolean(System.getProperty("testconfig.cleanupAfter", "true"));
}
