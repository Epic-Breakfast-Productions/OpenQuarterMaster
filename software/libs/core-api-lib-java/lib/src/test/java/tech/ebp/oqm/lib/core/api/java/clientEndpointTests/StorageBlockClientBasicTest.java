package tech.ebp.oqm.lib.core.api.java.clientEndpointTests;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.java.OqmCoreApiClient;
import tech.ebp.oqm.lib.core.api.java.search.QueryParams;
import tech.ebp.oqm.lib.core.api.java.testUtils.testClases.JwtAuthTest;
import tech.ebp.oqm.lib.core.api.java.utils.jackson.JacksonUtils;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class StorageBlockClientBasicTest extends JwtAuthTest {
	
	@BeforeAll
	public static void setup() {
		setupAndStart();
	}
	
	@Test
	void testStorageBlockSearch() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig().build())
									  .build();
		
		HttpResponse<ObjectNode> response = client.storageBlockSearch(this.getCredentials(), "default", new QueryParams()).join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body().toPrettyString());
	}
	
	@Test
	void testStorageBlockAdd() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig().build())
									  .build();
		
		ObjectNode newStorageBlock = JacksonUtils.MAPPER.createObjectNode();
		
		newStorageBlock.put("label", "testStorageBlock");
		
		HttpResponse<ObjectNode> response = client.storageBlockAdd(this.getCredentials(), "default", newStorageBlock).join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code: " + response.body());
		System.out.println(response.body());
	}
	
	@Test
	void testStorageBlockGet() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig().build())
									  .build();
		
		ObjectNode newStorageBlock = JacksonUtils.MAPPER.createObjectNode();
		
		newStorageBlock.put("label", "testStorageBlock2");
		
		HttpResponse<ObjectNode> response = client.storageBlockAdd(this.getCredentials(), "default", newStorageBlock).join();
		String id = response.body().get("id").asText();
		assertEquals(200, response.statusCode(), "Unexpected response code: " + response.body());
		
		System.out.println("Created Storage Block ID: " + id);
		
		HttpResponse<ObjectNode> getResponse = client.storageBlockGet(this.getCredentials(), "default", id).join();
		assertEquals(200, getResponse.statusCode(), "Unexpected response code: " + getResponse.body());
		
		System.out.println(getResponse.body());
	}
	
}