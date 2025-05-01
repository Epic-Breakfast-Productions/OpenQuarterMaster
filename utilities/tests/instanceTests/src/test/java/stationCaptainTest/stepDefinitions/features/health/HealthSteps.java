package stationCaptainTest.stepDefinitions.features.health;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.scenarioUtils.AttachUtils;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static stationCaptainTest.testResources.rest.RestHelpers.NULL_CERT_TRUST_MANAGER_CLIENT_BUILDER;

@Slf4j
public class HealthSteps extends BaseStepDefinitions {
	
	public HealthSteps(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
		log.debug("scenario: {}", this.getScenario());
	}
	
	@When("the health check call to {string} at path {string} is made")
	public void theHealthCheckCallToOnPortServicePortIsMade(String healthEndpoint, String servicePath) throws URISyntaxException, IOException, InterruptedException {
		HttpClient client = NULL_CERT_TRUST_MANAGER_CLIENT_BUILDER.build();
		
		URI uri = CONFIG.getInstance().getUri(servicePath, healthEndpoint);
		
		log.info("Hitting health uri at: {}", uri);
		
		HttpRequest request = HttpRequest.newBuilder()
								  .uri(uri)
								  .build();
		
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		this.getContext().getData().put("healthResponse", response);
	}
	
	@Then("the result of the healthcheck shows {string} is running")
	public void theResultOfTheHealthcheckShowsIsRunning(String serviceName) throws IOException {
		HttpResponse<String> httpResponse = (HttpResponse<String>) this.getContext().getData().get("healthResponse");
		
		log.debug("scenario: {}", this.getScenario());
		
		AttachUtils.attach(httpResponse, "Health Check Response", this.getScenario());
		
		assertEquals(200, httpResponse.statusCode());
	}
	
}
