package stationCaptainTest.lifecycle;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

import java.io.IOException;
import java.time.Duration;

@Slf4j
public class LifecycleEvents extends BaseStepDefinitions {
	
	public LifecycleEvents(TestContext context) {
		super(context);
	}
	
	@Before
	@Override
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
		
		Duration waitDuration = CONFIG.getTestSpacerWait();
		log.info("Doing a {} wait to even out between tests.", waitDuration);
		try {
			Thread.sleep(waitDuration.toMillis());
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.debug("Done waiting between tests.");
	}
	
	@BeforeAll
	public static void logStart(){
		log.info("STARTING tests.");
	}
	
	@After
	public void cleanup() throws IOException {
		log.info("Cleaning up after test.");
		
		//TODO:: remove data from instance
		
		this.getContext().close();
		log.info("Done Cleaning up after test.");
	}
}
