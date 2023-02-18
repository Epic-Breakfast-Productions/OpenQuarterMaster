package stationCaptainTest.stepDefinitions.features;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Installers extends BaseStepDefinitions {
	
	private static final Path INSTALLER_OUTPUT_DIR = Path.of("..", "bin");
	
	public Installers(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	@When("the command to make the installers are made")
	public void the_command_to_make_the_installers_are_made() throws InterruptedException, IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("../makeInstallers.sh");
		
		ShellProcessResults results = ShellProcessResults.builderFromProcessBuilder(processBuilder).build();
		
		this.getScenario().attach(results.getExitCode() + "", "text/plain", "Exit code from command.");
		this.getScenario().attach(results.getStdOut(), "text/plain", "Std out from command");
		this.getScenario().attach(results.getErrOut(), "text/plain", "Error out from command");
		
		this.getContext().setShellProcessResults(results);
	}
	
	@Then("the following installers were created:")
	public void the_following_installers_were_created(List<String> typesToFind) throws IOException {
		for (String curType : typesToFind) {
			try (
				Stream<Path> found = Files.find(
					INSTALLER_OUTPUT_DIR,
					Integer.MAX_VALUE,
					(path, basicFileAttributes)->path.toFile().getName().matches(".*." + curType)
				);
			) {
				assertTrue(found.findAny().isPresent(), "Installer not found for ." + curType);
			}
		}
	}
}
