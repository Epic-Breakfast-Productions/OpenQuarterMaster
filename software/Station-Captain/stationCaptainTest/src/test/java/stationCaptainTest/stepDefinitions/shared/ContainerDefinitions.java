package stationCaptainTest.stepDefinitions.shared;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;
import stationCaptainTest.scenarioUtils.AttachUtils;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ContainerDefinitions extends BaseStepDefinitions {
	public ContainerDefinitions(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	@When("the {string} command is made on the running container")
	public void theCommandIsMade(String command) throws IOException, InterruptedException {
		Container.ExecResult result = this.getContext().getRunningContainer().execInContainer(command.split(" "));
		
		AttachUtils.attach(result, "Command run", this.getScenario());
		this.getContext().setContainerExecResult(result);
	}
	
	@Then("command from the container returns successfully")
	public void command_returns_successfully() throws InterruptedException {
//		log.info("Waiting.");
//		Thread.sleep(5*60*1000);
		assertEquals(0, this.getContext().getContainerExecResult().getExitCode());
	}
	
}
