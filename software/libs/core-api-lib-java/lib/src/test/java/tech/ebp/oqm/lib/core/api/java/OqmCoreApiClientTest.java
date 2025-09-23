package tech.ebp.oqm.lib.core.api.java;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import tech.ebp.oqm.lib.core.api.java.auth.OqmCredentials;
import tech.ebp.oqm.lib.core.api.java.config.CoreApiConfig;
import tech.ebp.oqm.lib.core.api.java.testUtils.CoreAPITestContainerUtils;
import tech.ebp.oqm.lib.core.api.java.testUtils.JwtUtils;
import tech.ebp.oqm.lib.core.api.java.testUtils.OqmCoreApiWebServiceContainer;
import tech.ebp.oqm.lib.core.api.java.testUtils.RunningServerTest;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * TODO:: Figure out credentials. need to swap to jwt-driven. Create jwt here?
 *
 */
class OqmCoreApiClientTest extends RunningServerTest {
	private static MongoDBContainer mongoDBContainer = CoreAPITestContainerUtils.getMongoContainer();
	private static OqmCoreApiWebServiceContainer coreApiContainer = CoreAPITestContainerUtils.getCoreApiContainer().setupForPlainJwtAuth();
	
	OqmCredentials creds = JwtUtils.generateJwtCreds(true);
	
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
	
	@Test
	void testGetGeneralId() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig())
									  .build();
		
		System.out.println("creds: " + creds.getAccessHeaderContent());
		
		HttpResponse<ObjectNode> response = client.generalIdValidateGet(creds, "ISBN_13", "9780691165615").join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body().toPrettyString());
	}
	
	@Test
	void testGetInteractingEntitySelf() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig())
									  .build();
		
		System.out.println("creds: \"" + creds.getAccessHeaderContent() + "\"");
		
		HttpResponse<String> response = client.interactingEntityGetSelf(creds).join();
		
		System.out.println(response.body());
		assertEquals(200, response.statusCode(), "Unexpected response code.");
	}
}