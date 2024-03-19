package stationCaptainTest.testResources.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class ConfigReader {
	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
	protected static final String CONFIG_FILE_PROP_NAME = "TEST_CONFIG_FILE";
	protected static final String DEFAULT_CONFIG_LOCATION = "config.yaml";
	
	private static TestRunConfig MAIN_TEST_RUN_CONFIG;
	
	private static InstanceConnectionConfig getSetupConfig() {
		return null;
	}
	
	public static synchronized TestRunConfig getTestRunConfig() throws IOException {
		if (MAIN_TEST_RUN_CONFIG != null) {
			return MAIN_TEST_RUN_CONFIG;
		}
		
		log.info("Setting up new TestRunConfig");
		String configLocaleLocation = System.getProperty(CONFIG_FILE_PROP_NAME, DEFAULT_CONFIG_LOCATION);
		File configLocaleFile = new File(configLocaleLocation);
		
		if(DEFAULT_CONFIG_LOCATION.equals(configLocaleLocation) && !configLocaleFile.exists()){
			log.info("No default config file present. Returning default config.");
			MAIN_TEST_RUN_CONFIG = new TestRunConfig();
		} else {
			MAIN_TEST_RUN_CONFIG = OBJECT_MAPPER.readValue(configLocaleFile, TestRunConfig.class);
		}
		
		return MAIN_TEST_RUN_CONFIG;
	}
	
}
