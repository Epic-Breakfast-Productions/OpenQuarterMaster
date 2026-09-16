package stationCaptainTest.stepDefinitions.shared;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testcontainers.containers.Container;
import stationCaptainTest.scenarioUtils.AttachUtils;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;
import stationCaptainTest.testResources.snhConnector.CommandResult;

import java.io.IOException;

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
	
	@When("the {string} command is made")
	public void theCommandIsMade(String command) throws IOException, InterruptedException {
		CommandResult result = this.getContext().getSnhConnector().runCommand(command.split(" "));
		
		AttachUtils.attach(result, "Command run", this.getScenario());
		this.getContext().setCommandResult(result);
	}
	
	@Then("command returns successfully")
	public void command_returns_successfully() {
		assertEquals(0, this.getContext().getCommandResult().getReturnCode());
	}
	
}
