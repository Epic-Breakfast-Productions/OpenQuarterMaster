package com.ebp.openQuarterMaster.driverServer.acceptance;

import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkiverse.cucumber.CucumberQuarkusIntegrationTest;
import io.quarkiverse.cucumber.CucumberQuarkusTest;
import io.quarkiverse.cucumber.CucumberQuarkusTestCoptInt;

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
public class CucumberIntTestConfig extends CucumberQuarkusTestCoptInt {

}
