package stationCaptainTest.stepDefinitions.features;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testcontainers.containers.Container;
import stationCaptainTest.constants.FileLocationConstants;
import stationCaptainTest.scenarioUtils.AttachUtils;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;
import stationCaptainTest.testResources.containerUtils.ContainerUtils;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstallerSteps extends BaseStepDefinitions {
	
	private static ShellProcessResults MAKE_INSTALLER_RESULTS;
	
	private static synchronized ShellProcessResults makeInstallers() throws IOException, InterruptedException {
		if(MAKE_INSTALLER_RESULTS == null){
			MAKE_INSTALLER_RESULTS = ShellProcessResults.builderFromProcessBuilder(
				new ProcessBuilder(FileLocationConstants.MAKE_INSTALLERS_SH)
			).build();
		}
		
		return MAKE_INSTALLER_RESULTS;
	}
	
	public InstallerSteps(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	@Given("the host is setup for install")
	public void the_host_is_setup_for_install() {
		this.getContext().getSnhConnector().init(false);
	}
	
	@When("the command to install OQM is made")
	public void the_command_to_install_oqm_is_made() {
		this.getContext().getSnhConnector().installOqm();
	}
	
	@Then("OQM is running")
	public void oqm_is_running() {
		//TODO:: verify
	}
	
	
}
