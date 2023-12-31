package stationCaptainTest.stepDefinitions.features;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.constants.FileLocationConstants;
import stationCaptainTest.scenarioUtils.AttachUtils;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;
import stationCaptainTest.testResources.snhConnector.CommandResult;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
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
		CommandResult result = this.getContext().getSnhConnector().installOqm(false);
		AttachUtils.attach(result, "OQM install step", this.getScenario());
		if(result.getReturnCode() != 0){
			throw new RuntimeException("FAILED to install OQM: " + result.getStdErr());
		}
	}
	
	@Then("OQM is running")
	public void oqm_is_running() {
		CommandResult dockerStatsResult = this.getContext().getSnhConnector().runCommand("docker", "stats", "--no-stream");
		AttachUtils.attach(dockerStatsResult, "Running containers", this.getScenario());
		CommandResult systemdListResult = this.getContext().getSnhConnector().runCommand("systemctl", "list-units", "--no-legend", "open\\\\x2bquarter\\\\x2bmaster\\\\x2d*");
		AttachUtils.attach(systemdListResult, "OQM related systemd units", this.getScenario());
		
		for(
			String curSystemdUnit :
			systemdListResult.getStdOut().split("\n")
		){
			String[] unitStatusParts = curSystemdUnit.substring(2).split("\\s+");
			log.debug("Systemd unit status parts: {}", (Object) unitStatusParts);
			String active = unitStatusParts[2];
			String status = unitStatusParts[3];
			
			assertEquals("active", active, "Systemd unit " + unitStatusParts[1] + " not running appropriately.");
			assertEquals("running", status, "Systemd unit " + unitStatusParts[1] + " not running appropriately.");
		}
	}
	
	
}
