package stationCaptainTest.testResources.threads;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import stationCaptainTest.testResources.Utils;
import stationCaptainTest.testResources.config.ConfigReader;
import stationCaptainTest.testResources.rest.RestHelpers;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static stationCaptainTest.testResources.Utils.FAKER;

@Slf4j
@Data
@Builder
public class PerformanceTestThread implements Callable<PerformanceTestResult> {
	
	int coreApiPort;
	int threadNum;
	int numStorageBlocks;
	int numItems;
	int numUpdates;
	
	@Override
	public PerformanceTestResult call() throws Exception {
		PerformanceTestResult.PerformanceTestResultBuilder outputBuilder = PerformanceTestResult.builder().threadNum(this.threadNum);
		HttpClient client = RestHelpers.NULL_CERT_TRUST_MANAGER_CLIENT_BUILDER.build();
		long numCalls = 0;
		long numErrors = 0;
		StopWatch overallWatch = StopWatch.createStarted();
		
		List<String> storageBlocks = new ArrayList<>(this.numStorageBlocks);
		List<String> items = new ArrayList<>(this.numItems);
		
		{
			String blockPrefix = "Block-" + threadNum + "-";
			ObjectNode newStorageBlockObj = Utils.OBJECT_MAPPER.createObjectNode()
												.put("description", FAKER.lorem().paragraph());
			for (int i = 0; i < this.numStorageBlocks; i++) {
				newStorageBlockObj.put("label", blockPrefix + i)
					.put("location", FAKER.address().fullAddress());
				HttpRequest request = HttpRequest.newBuilder()
										  .uri(ConfigReader.getTestRunConfig().getInstance().getUri(coreApiPort, "/api/v1/inventory/storage-block"))
										  .header("Authorization", RestHelpers.getClientCredentialString())
										  .POST(HttpRequest.BodyPublishers.ofString(newStorageBlockObj.toPrettyString()))
										  .build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				numCalls++;
				if(response.statusCode() != 200){
					numErrors++;
				}
				
				String newId = response.body();
				storageBlocks.add(newId);
				log.debug("Created storage block with id: {}", newId);
			}
		}
		{
			String itemPrefix = "Item-" + threadNum + "-";
			ObjectNode itemObj = Utils.OBJECT_MAPPER.createObjectNode()
												.put("description", FAKER.lorem().paragraph());
			for (int i = 0; i < this.numStorageBlocks; i++) {
				itemObj.put("name", itemPrefix + i)
					.put("location", FAKER.address().fullAddress());
				HttpRequest request = HttpRequest.newBuilder()
										  .uri(ConfigReader.getTestRunConfig().getInstance().getUri(coreApiPort, "/api/v1/inventory/item"))
										  .header("Authorization", RestHelpers.getClientCredentialString())
										  .POST(HttpRequest.BodyPublishers.ofString(itemObj.toPrettyString()))
										  .build();
				numCalls++;
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				String newId = response.body();
				storageBlocks.add(newId);
				log.debug("Created item with id: {}", newId);
			}
		}
		
		//TODO:: other object types?
		
		//TODO:: update objects
		
		overallWatch.stop();
		log.info("Performance test thread {} took {} to complete.", this.threadNum, overallWatch.formatTime());
		outputBuilder.overallDuration(Duration.of(overallWatch.getTime(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS));
		outputBuilder.numCalls(numCalls);
		return outputBuilder.build();
	}
}
