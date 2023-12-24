package stationCaptainTest.testResources.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.testResources.config.snhSetup.SnhSetupConfig;

@Slf4j
public class ConfigReader {
	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	protected static final String CONFIG_FILE_PROP_NAME = "TEST_CONFIG_FILE";
	protected static final String DEFAULT_CONFIG_LOCATION = "config.json";
	
	private static TestRunConfig MAIN_TEST_RUN_CONFIG;
	
	
	private static SnhSetupConfig getSetupConfig() {
		return null;
	}
	
	public static synchronized TestRunConfig getTestRunConfig() {
		if (MAIN_TEST_RUN_CONFIG != null) {
			return MAIN_TEST_RUN_CONFIG;
		}
		
		log.info("Setting up new TestRunConfig");
		TestRunConfig.TestRunConfigBuilder builder = TestRunConfig.builder();
		builder.setupConfig(getSetupConfig());
		
		MAIN_TEST_RUN_CONFIG = builder.build();
		return MAIN_TEST_RUN_CONFIG;
	}
	
}
