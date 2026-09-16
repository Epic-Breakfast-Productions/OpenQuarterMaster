package tech.ebp.oqm.lib.core.api.java;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.java.search.QueryParams;
import tech.ebp.oqm.lib.core.api.java.testUtils.testClases.JwtAuthTest;
import tech.ebp.oqm.lib.core.api.java.utils.jackson.JacksonUtils;

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
	
	@Test
	void testCreateItem() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig().build())
									  .build();
		
		ObjectNode newItem = JacksonUtils.MAPPER.createObjectNode();
		
		newItem.put("name", "testItem");
		newItem.put("storageType", "BULK");
		newItem.putObject("unit").put("string", "units");
		
		HttpResponse<ObjectNode> response = client.invItemCreate(this.getCredentials(), "default", newItem).join();
		
		System.out.println(response.body());
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		
		
		HttpResponse<ObjectNode> searchAllResponse = client.invItemSearch(this.getCredentials(), "default", new QueryParams()).join();
		System.out.println(searchAllResponse.body());
		assertEquals(200, searchAllResponse.statusCode(), "Unexpected response code.");
		
		HttpResponse<ObjectNode> searchOneResponse = client.invItemSearch(this.getCredentials(), "default", new QueryParams().addParam("name", "testItem")).join();
		System.out.println(searchOneResponse.body());
		assertEquals(200, searchOneResponse.statusCode(), "Unexpected response code.");
		
		HttpResponse<ObjectNode> searchNoneResponse = client.invItemSearch(this.getCredentials(), "default", new QueryParams().addParam("name", "foo")).join();
		System.out.println(searchNoneResponse.body());
		assertEquals(200, searchNoneResponse.statusCode(), "Unexpected response code.");
		
		HttpResponse<ObjectNode> getResponse = client.invItemGet(this.getCredentials(), "default", response.body().get("id").asText()).join();
		System.out.println(getResponse.body());
		assertEquals(200, getResponse.statusCode(), "Unexpected response code.");
	}
	
	@Test
	void testTransaction() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig().build())
									  .build();
		
		ObjectNode storageBlock = JacksonUtils.MAPPER.createObjectNode();
		storageBlock.put("label", "testBlock");
		HttpResponse<ObjectNode> response = client.storageBlockAdd(this.getCredentials(), "default", storageBlock).join();
		assertEquals(200, response.statusCode(), "Unexpected response code in setup.");
		storageBlock = response.body();
		
		
		
		
		ObjectNode newItem = JacksonUtils.MAPPER.createObjectNode();
		newItem.put("name", "testItem");
		newItem.put("storageType", "BULK");
		newItem.putObject("unit").put("string", "units");
		newItem.putArray("storageBlocks").add(storageBlock.get("id").asText());
		
		response = client.invItemCreate(this.getCredentials(), "default", newItem).join();
		
		System.out.println(response.body());
		assertEquals(200, response.statusCode(), "Unexpected response code in setup.");
		newItem = response.body();
		
		
		ObjectNode transaction = JacksonUtils.MAPPER.createObjectNode();
		transaction.put("block", storageBlock.get("id").asText());
		transaction.putObject("amount").put("value", 0)
			.put("scale", "ABSOLUTE")
			.putObject("unit").put("string", "units");
		transaction.put("type", "SET_AMOUNT");
		
		
		HttpResponse<String> tResponse = client.invItemStoredTransact(this.getCredentials(), "default", newItem.get("id").asText(), transaction).join();
		
		System.out.println(tResponse.body());
		assertEquals(200, tResponse.statusCode(), "Unexpected response code.");
		
	}
}