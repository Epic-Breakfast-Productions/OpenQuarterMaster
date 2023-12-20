package stationCaptainTest.stepDefinitions.features;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.testcontainers.utility.MountableFile;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ConfigUtilitySteps extends BaseStepDefinitions {
	private static final String TEST_VALUES = "src/test/resources/res/config-util/99-test-values.json";
	private static final String TEST_VALUES_DEST = "/etc/oqm/config/configs/99-test-values.json";
	private static final String TEST_TEMPLATE = "src/test/resources/res/config-util/configTemplate.list";
	private static final String TEST_TEMPLATE_DEST = "/tmp/configTemplate.list";
	
	
	public ConfigUtilitySteps(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	
	@And("test config files exist for testing config utility")
	public void testConfigFilesExistForTestingConfigUtility() {
		log.info("Adding test config files to container.");
		this.getContext().getRunningContainer().copyFileToContainer(MountableFile.forHostPath(TEST_VALUES), TEST_VALUES_DEST);
		this.getContext().getRunningContainer().copyFileToContainer(MountableFile.forHostPath(TEST_TEMPLATE), TEST_TEMPLATE_DEST);
	}
	
	
	@And("the configurations are listed as output")
	public void theConfigurationsAreListedAsOutput() throws JsonProcessingException {
		String rawOutput = this.getContext().getContainerExecResult().getStdout();
		ObjectNode object = (ObjectNode) OBJECT_MAPPER.readTree(rawOutput);
		
		assertTrue(object.has("captain"));
		assertTrue(object.has("test"));
	}
	
	@And("the configuration value {string} was returned")
	public void theConfigurationValueWasReturned(String expectedOutput) {
		String rawOutput = this.getContext().getContainerExecResult().getStdout().trim();
		
		assertEquals(expectedOutput, rawOutput);
	}
	
	@And("the config template data returned has the placeholders filled")
	public void theConfigTemplateDataReturnedHasThePlaceholdersFilled() {
		String rawOutput = this.getContext().getContainerExecResult().getStdout().trim();
		
		assertFalse(rawOutput.matches("\\{(.*)}"));
	}
	
	@And("the config command outputs about the file not found")
	public void theConfigCommandOutputsAboutTheFileNotFound() {
		String rawOutput = this.getContext().getContainerExecResult().getStderr().trim();
		
		assertTrue(rawOutput.contains("Failed to read file"));
		assertTrue(rawOutput.contains("No such file or directory"));
	}
	
	@And("the config command outputs about the config key {string} not found")
	public void theConfigCommandOutputsAboutTheConfigKeyNotFound(String configKey) {
		String rawOutput = this.getContext().getContainerExecResult().getStderr().trim();
		
		assertTrue(rawOutput.contains("Config key not found"));
		assertTrue(rawOutput.contains(configKey));
	}
	
	@And("the json output matches {string}")
	public void theJsonOutputMatches(String expectedJson) throws JsonProcessingException {
		String rawOutput = this.getContext().getContainerExecResult().getStdout().trim();
		
		ObjectNode expected = (ObjectNode) OBJECT_MAPPER.readTree(expectedJson);
		ObjectNode actual = (ObjectNode) OBJECT_MAPPER.readTree(rawOutput);
		
		assertEquals(expected, actual);
		
		this.getContext().getData().put("expectedJson", expected);
	}
	
	@And("the default addendum config file is updated")
	public void theDefaultAddendumConfigFileIsUpdated() {
		ObjectNode expected = (ObjectNode) this.getContext().getData().get("expectedJson");
		
		ObjectNode actual = (ObjectNode) this.getContext().getRunningContainer().copyFileFromContainer("/etc/oqm/config/configs/99-custom.json", inputStream -> {
			try(ByteArrayOutputStream output = new ByteArrayOutputStream()) {
				IOUtils.copy(inputStream, output);
				return OBJECT_MAPPER.readTree(output.toByteArray());
			}
		});
		assertEquals(expected, actual);
	}
}
