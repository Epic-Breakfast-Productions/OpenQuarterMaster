package stationCaptainTest.stepDefinitions.shared;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

public class ContainerDefinitions extends BaseStepDefinitions {
	public ContainerDefinitions(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	
}
