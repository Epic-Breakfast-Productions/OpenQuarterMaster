package stationCaptainTest.lifecycle;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;
import stationCaptainTest.testResources.config.ConfigReader;
import stationCaptainTest.testResources.rest.RestHelpers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
		
		
	}
	
	@BeforeAll
	public static void logStart(){
		log.info("STARTING tests.");
	}
	
	@After
	public void cleanup() throws IOException, URISyntaxException, InterruptedException {
		log.info("Cleaning up after test.");
		
		HttpClient client = RestHelpers.NULL_CERT_TRUST_MANAGER_CLIENT_BUILDER.build();
		HttpRequest request = HttpRequest.newBuilder()
								  .uri(ConfigReader.getTestRunConfig().getInstance().getUri(9001, "/api/v1/inventory/manage/clearDb"))
								  .header("Authorization", RestHelpers.getClientCredentialString())
								  .DELETE()
								  .build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		log.info("Response from clearing database: {} / {}", response, response.body());
		
		this.getContext().close();
		log.info("Done Cleaning up after test.");
	}
}
