package stationCaptainTest.lifecycle;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

import java.io.IOException;

@Slf4j
public class LifecycleEvents extends BaseStepDefinitions {
	
	public LifecycleEvents(TestContext context) {
		super(context);
	}
	
	@Before
	@Override
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	@BeforeAll
	public static void logStart(){
		log.info("STARTING tests.");
	}
	
	@After
	public void cleanup() throws IOException {
		log.info("Cleaning up after test.");
		this.getContext().close();
		log.info("Done Cleaning up after test.");
	}
}
