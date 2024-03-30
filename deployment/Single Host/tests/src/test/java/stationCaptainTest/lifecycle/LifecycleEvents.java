package stationCaptainTest.lifecycle;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.scenarioUtils.AttachUtils;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;
import stationCaptainTest.testResources.snhConnector.CommandResult;
import stationCaptainTest.testResources.snhConnector.SnhConnector;

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
		CommandResult result = this.getContext().getSnhConnector().runCommand("oqm-captain", "--package-logs");
		log.debug("return code of getting logs packaged: {}", result.getReturnCode());
		log.debug("output of getting logs packaged: {}", result.getStdOut());
		log.debug("error of getting logs packaged: {}", result.getStdErr());
		if(result.getReturnCode() == 0){
			AttachUtils.attachRemoteFile(
				result.getStdOut().trim(),
				"Log bundle",
				this.getScenario(),
				this.getContext().getSnhConnector()
			);
		}
		
		log.info("Cleaning up after test.");
		
		if(CONFIG.isCleanupAfter() && this.getContext().getSnhConnector() != null){
			log.info("Clearing host of OQM after install.");
			this.getContext().getSnhConnector().uninstallOqm();
		}
		
		this.getContext().close();
		log.info("Done Cleaning up after test.");
	}
}
