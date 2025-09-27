package tech.ebp.oqm.lib.core.api.java;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.java.testUtils.testClases.JwtAuthTest;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class OqmCoreApiClientBasicTest extends JwtAuthTest {
	
	
	@BeforeAll
	public static void setup() {
		setupAndStart();
	}
	
	@Test
	void testGetServerHealth() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig().build())
									  .build();
		
		HttpResponse<ObjectNode> response = client.serverHealthGet().join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body().toPrettyString());
	}
	
	@Test
	void testGetCurrency() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig().build())
									  .build();
		
		HttpResponse<String> response = client.infoCurrencyGet().join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body());
	}
	
	@Test
	void testGetGeneralId() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig().build())
									  .build();
		
		HttpResponse<ObjectNode> response = client.generalIdValidateGet(this.getCredentials(), "ISBN_13", "9780691165615").join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body().toPrettyString());
	}
	
	@Test
	void testGetInteractingEntitySelf() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig().build())
									  .build();
		
		HttpResponse<String> response = client.interactingEntityGetSelf(this.getCredentials()).join();
		
		System.out.println(response.body());
		assertEquals(200, response.statusCode(), "Unexpected response code.");
	}
}