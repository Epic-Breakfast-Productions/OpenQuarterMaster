package stationCaptainTest.stepDefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.*;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExampleStepDefinitions extends BaseStepDefinitions {
	
	public ExampleStepDefinitions(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	@Given("an example scenario")
	public void anExampleScenario() {
		assertNotNull(this.getContext());
	}
	
	@When("all step definitions are implemented")
	public void allStepDefinitionsAreImplemented() throws InterruptedException {
//		Thread.sleep(5_000);
	}
	
	@Then("the scenario passes")
	public void theScenarioPasses() {
	}
	
}
