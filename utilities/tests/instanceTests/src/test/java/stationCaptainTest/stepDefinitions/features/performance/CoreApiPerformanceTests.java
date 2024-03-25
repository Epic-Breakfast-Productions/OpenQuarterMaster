package stationCaptainTest.stepDefinitions.features.performance;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.scenarioUtils.AttachUtils;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;
import stationCaptainTest.testResources.threads.PerformanceTestResult;
import stationCaptainTest.testResources.threads.PerformanceTestThread;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class CoreApiPerformanceTests extends BaseStepDefinitions {
	private static final String CORE_API_PORT_KEY = "coreApiKey";
	private static final String NUM_CLIENTS_KEY = "numClients";
	private static final String NUM_BLOCKS_KEY = "numStorageBlocks";
	private static final String NUM_ITEMS_KEY = "numItems";
	private static final String NUM_UPDATES_KEY = "numUpdates";
	private static final String RESULTS_KEY = "processingResults";
	
	public CoreApiPerformanceTests(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	@Given("the core api is on port {int}")
	public void theCoreApiIsOnPort(int coreApiPort) {
		this.getContext().getData().put(CORE_API_PORT_KEY, coreApiPort);
	}
	
	@Given("we are using {int} clients")
	public void weAreUsingNumClientsClients(int numClients) {
		this.getContext().getData().put(NUM_CLIENTS_KEY, numClients);
	}
	
	@And("each client is creating {int} storage blocks")
	public void eachClientIsCreatingNumStorageBlocksStorageBlocks(int numStorageBlocks) {
		this.getContext().getData().put(NUM_BLOCKS_KEY, numStorageBlocks);
	}
	
	@And("each client is creating {int} items")
	public void eachClientIsCreatingNumItemsItems(int numItems) {
		this.getContext().getData().put(NUM_ITEMS_KEY, numItems);
	}
	
	@And("each client is performing {int} updates to each object")
	public void eachClientIsPerformingNumUpdatesUpdatesToEachObject(int numUpdates) {
		this.getContext().getData().put(NUM_UPDATES_KEY, numUpdates);
	}
	
	@When("the clients perform their actions")
	public void theClientsPerformTheirActions() throws ExecutionException, InterruptedException {
		int numClients = (int)this.getContext().getData().get(NUM_CLIENTS_KEY);
		
		LinkedList<Future<PerformanceTestResult>> resultFutures = new LinkedList<>();
		{
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numClients);
			PerformanceTestThread.PerformanceTestThreadBuilder threadBuilder = PerformanceTestThread.builder()
																				   .coreApiPort((Integer) this.getContext().getData().get(CORE_API_PORT_KEY))
																				   .numStorageBlocks((Integer) this.getContext().getData().get(NUM_BLOCKS_KEY))
																				   .numItems((Integer) this.getContext().getData().get(NUM_ITEMS_KEY))
																				   .numUpdates((Integer) this.getContext().getData().get(NUM_UPDATES_KEY));
			for (int i = 1; i <= numClients; i++) {
				threadBuilder.threadNum(i);
				resultFutures.add(executor.submit(threadBuilder.build()));
			}
		}
		ArrayList<PerformanceTestResult> results = new ArrayList<>();
		while(!resultFutures.isEmpty()){
			Future<PerformanceTestResult> future = resultFutures.pop();
			PerformanceTestResult result = future.get();
			results.add(result);
		}
		
		this.getContext().getData().put(RESULTS_KEY, results);
	}
	
	@Then("all requests returned successfully")
	public void allRequestsReturnedSuccessfully() {
		List<PerformanceTestResult> performanceTestResult = (List<PerformanceTestResult>) this.getContext().getData().get(RESULTS_KEY);
		AttachUtils.attach(performanceTestResult, "Performance Test Results", this.getScenario());
		
		for(PerformanceTestResult curResult : performanceTestResult){
			assertEquals(0, curResult.getNumErrors());
		}
	}
	
	@Given("a {string} buffer between tests has occurred")
	public void aBufferBetweenTestsHasOccurred(String bufferTime) {
		Duration waitDuration = Duration.parse("PT"+bufferTime);
		log.info("Doing a {} wait to even out between tests.", waitDuration);
		try {
			Thread.sleep(waitDuration.toMillis());
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.debug("Done waiting between tests.");
	}
}
