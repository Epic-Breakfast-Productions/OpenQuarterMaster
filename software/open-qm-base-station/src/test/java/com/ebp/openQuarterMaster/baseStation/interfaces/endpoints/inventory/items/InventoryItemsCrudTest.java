package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.inventory.items;

import com.ebp.openQuarterMaster.baseStation.service.JwtService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.testResources.data.InventoryItemTestObjectCreator;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.storage.items.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.ListAmountItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.SimpleAmountItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.TrackedItem;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static com.ebp.openQuarterMaster.baseStation.testResources.TestRestUtils.setupJwtCall;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
@Slf4j
@QuarkusTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class)
@TestHTTPEndpoint(InventoryItemsCrud.class)
class InventoryItemsCrudTest extends RunningServerTest {
	
	@Inject
	InventoryItemTestObjectCreator testObjectCreator;
	
	@Inject
	ObjectMapper objectMapper;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@Inject
	JwtService jwtService;
	
	@Inject
	TestUserService testUserService;
	
	private ObjectId create(User user, InventoryItem item) throws JsonProcessingException {
		ValidatableResponse response = setupJwtCall(given(), this.jwtService.getUserJwt(user, false).getToken())
			.contentType(ContentType.JSON)
			.body(objectMapper.writeValueAsString(item))
			.when()
			.post()
			.then();
		
		response.statusCode(Response.Status.CREATED.getStatusCode());
		
		ObjectId returned = response.extract().body().as(ObjectId.class);
		
		log.info("Got object id back from create request: {}", returned);
		return returned;
	}
	
	@Test
	public void testCreateSimpleAmountItem() throws JsonProcessingException {
		User user = this.testUserService.getTestUser(false, true);
		SimpleAmountItem item = (SimpleAmountItem) new SimpleAmountItem().setName(FAKER.commerce().productName());
		ObjectId returned = create(user, item);
		
		InventoryItem stored = inventoryItemService.get(returned);
		assertNotNull(stored);
		
		assertFalse(stored.getHistory().isEmpty());
		assertEquals(1, stored.getHistory().size());
		
		item.setHistory(stored.getHistory());
		item.setId(returned);
		
		
		assertEquals(item, stored);
	}
	
	@Test
	public void testCreateListAmountItem() throws JsonProcessingException {
		User user = this.testUserService.getTestUser(false, true);
		ListAmountItem item = (ListAmountItem) new ListAmountItem().setName(FAKER.commerce().productName());
		ObjectId returned = create(user, item);
		
		InventoryItem stored = inventoryItemService.get(returned);
		assertNotNull(stored);
		
		assertFalse(stored.getHistory().isEmpty());
		assertEquals(1, stored.getHistory().size());
		
		item.setHistory(stored.getHistory());
		item.setId(returned);
		
		assertEquals(item, stored);
	}
	
	@Test
	public void testCreateTrackedItem() throws JsonProcessingException {
		User user = this.testUserService.getTestUser(false, true);
		TrackedItem item = (TrackedItem) new TrackedItem()
			.setTrackedItemIdentifierName("id")
			.setName(FAKER.commerce().productName());
		ObjectId returned = create(user, item);
		
		InventoryItem stored = inventoryItemService.get(returned);
		assertNotNull(stored);
		
		assertFalse(stored.getHistory().isEmpty());
		assertEquals(1, stored.getHistory().size());
		
		item.setHistory(stored.getHistory());
		item.setId(returned);
		
		assertEquals(item, stored);
	}
}