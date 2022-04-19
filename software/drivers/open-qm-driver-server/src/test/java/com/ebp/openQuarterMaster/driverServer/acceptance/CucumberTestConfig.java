package com.ebp.openQuarterMaster.driverServer.acceptance;

import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkiverse.cucumber.CucumberQuarkusTest;


@CucumberOptions(
	features = "acceptanceFeatures",
	//        glue = {"com.acme"},
	//        extraGlue = {"com.acme.test.sharedSteps"},
	plugin = {
		"pretty",
		"json:build/test-results/test/cucumber/report.json",
		"html:build/test-results/test/cucumber/report.html",
	}
)
public class CucumberTestConfig extends CucumberQuarkusTest {
	public static void main(String[] args) {
		runMain(CucumberTestConfig.class, args);
	}
}
