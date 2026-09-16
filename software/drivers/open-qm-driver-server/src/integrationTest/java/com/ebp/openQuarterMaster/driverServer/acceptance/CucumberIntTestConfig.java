package com.ebp.openQuarterMaster.driverServer.acceptance;

import com.ebp.openQuarterMaster.driverServer.testUtils.lifecycleManagers.TestResourceLifecycleManager;
import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;

@CucumberOptions(
	features = "acceptanceFeatures",
	//        glue = {"com.acme"},
	//        extraGlue = {"com.acme.test.sharedSteps"},
	plugin = {
		"pretty",
		"json:build/test-results/quarkusIntTest/cucumber/report.json",
		"html:build/test-results/quarkusIntTest/cucumber/report.html",
	}
)
@QuarkusTestResource(
	value = TestResourceLifecycleManager.class,
	initArgs = {
		@ResourceArg(name = TestResourceLifecycleManager.OTHER_PORT_ARG, value = "true")
	}
)
public class CucumberIntTestConfig extends CucumberQuarkusIntegrationTest {
	
	public static void main(String[] args) {
		runMain(CucumberIntTestConfig.class, args);
	}
}
