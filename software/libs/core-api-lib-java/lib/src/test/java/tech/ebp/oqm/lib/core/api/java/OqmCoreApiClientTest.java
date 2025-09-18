package tech.ebp.oqm.lib.core.api.java;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.ebp.oqm.lib.core.api.java.config.CoreApiConfig;
import tech.ebp.oqm.lib.core.api.java.testUtils.CoreAPITestContainerUtils;
import tech.ebp.oqm.lib.core.api.java.testUtils.OqmCoreApiWebServiceContainer;
import tech.ebp.oqm.lib.core.api.java.testUtils.RunningServerTest;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OqmCoreApiClientTest extends RunningServerTest {
	private static MongoDBContainer mongoDBContainer = CoreAPITestContainerUtils.getMongoContainer();
	private static OqmCoreApiWebServiceContainer coreApiContainer = CoreAPITestContainerUtils.getCoreApiContainer().setupForBasicAuth();
	
	@BeforeAll
	public static void startContainers() {
		mongoDBContainer.start();
		coreApiContainer.start();
	}
	
	@AfterAll
	public static void stopContainers() {
		mongoDBContainer.start();
		coreApiContainer.start();
	}
	
	public CoreApiConfig getCoreApiConfig() {
		try {
			return CoreApiConfig.builder()
					   .baseUri(new URI("http://" + coreApiContainer.getHost() + ":" + coreApiContainer.getFirstMappedPort().toString()))
					   .build();
		} catch(URISyntaxException e) {
			throw new RuntimeException("Failed to create uri for core api.", e);
		}
	}
	
	@Test
	void testGetServerHealth() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig())
									  .build();
		
		HttpResponse<ObjectNode> response = client.serverHealthGet().join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body().toPrettyString());
	}
	
	@Test
	void testGetCurrency() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig())
									  .build();
		
		HttpResponse<String> response = client.infoCurrencyGet().join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body());
	}
}