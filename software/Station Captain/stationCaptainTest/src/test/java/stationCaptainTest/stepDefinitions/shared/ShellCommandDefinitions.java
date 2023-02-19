package stationCaptainTest.stepDefinitions.shared;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShellCommandDefinitions extends BaseStepDefinitions {
	
	public ShellCommandDefinitions(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	@Then("command returns successfully")
	public void command_returns_successfully() {
		assertEquals(0, this.getContext().getShellProcessResults().getExitCode());
	}
	
}
