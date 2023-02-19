package stationCaptainTest.stepDefinitions.features;

import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.Scenario;
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
	
	public InstallerSteps(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	@When("the command to make the installers are made")
	public void the_command_to_make_the_installers_are_made() throws InterruptedException, IOException {
		ShellProcessResults results = ShellProcessResults.builderFromProcessBuilder(
			new ProcessBuilder(FileLocationConstants.MAKE_INSTALLERS_SH)
		).build();
		
		AttachUtils.attach(results, "Build installers", this.getScenario());
		
		this.getContext().setShellProcessResults(results);
	}
	
	@Then("the following installers were created:")
	public void the_following_installers_were_created(List<String> typesToFind) throws IOException {
		for (String curType : typesToFind) {
			try (
				Stream<Path> found = Files.find(
					FileLocationConstants.INSTALLER_OUTPUT_DIR,
					Integer.MAX_VALUE,
					(path, basicFileAttributes)->path.toFile().getName().matches(".*." + curType)
				);
			) {
				assertTrue(found.findAny().isPresent(), "Installer not found for ." + curType);
			}
		}
	}
	
	@When("the {string} installer is installed on {string}")
	public void the_installer_is_installed(String installerType, String os) throws IOException, InterruptedException {
		this.getContext().setRunningContainer(ContainerUtils.startContainer(this.getContext(), installerType, os, false));
		
		this.getContext().setContainerExecResult(ContainerUtils.installStationCaptain(this.getContext(), this.getContext().getRunningContainer(), false));
	}
	
	@Then("the installer completed successfully")
	public void the_installer_completed_successfully() {
		AttachUtils.attach(this.getContext().getContainerExecResult(), "Installer run", this.getScenario());
		assertEquals(0, this.getContext().getContainerExecResult().getExitCode());
	}
	
	@Then("the {string} script is present")
	public void the_script_is_present(String script) throws IOException, InterruptedException {
		Container.ExecResult result = this.getContext().getRunningContainer().execInContainer(script, "-h");
		
		AttachUtils.attach(result, "Installed Script check", this.getScenario());
		
		assertEquals(0, result.getExitCode());
	}
	
}
