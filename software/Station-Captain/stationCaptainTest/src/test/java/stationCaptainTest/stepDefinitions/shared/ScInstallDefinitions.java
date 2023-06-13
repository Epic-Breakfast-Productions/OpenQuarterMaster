package stationCaptainTest.stepDefinitions.shared;

import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

import java.io.IOException;

public class ScInstallDefinitions extends BaseStepDefinitions {
	public ScInstallDefinitions(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	@Given("the {string} installer built and installed on {string}")
	public void the_installer_is_installed(String installerType, String os) throws IOException, InterruptedException {
		//TODO:: this
		throw new PendingException();
//		this.getContext().setRunningContainer(ContainerUtils.startContainer(installerType, os));
	}
	
}
