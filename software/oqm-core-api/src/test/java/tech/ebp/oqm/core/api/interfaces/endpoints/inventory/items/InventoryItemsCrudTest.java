package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.rest.dataImportExport.ImportBundleFileBody;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.ListAmountItem;
import tech.ebp.oqm.core.api.model.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.core.api.model.object.storage.items.TrackedItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items.InventoryItemsCrud;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

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
	@Location("templates/items.csv")
	Template itemsCsv;


	
	private ObjectId create(User user, InventoryItem item) throws JsonProcessingException {
		ValidatableResponse response = setupJwtCall(given(), user.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
										   .contentType(ContentType.JSON)
										   .body(objectMapper.writeValueAsString(item))
										   .when()
										   .post("", DEFAULT_TEST_DB_NAME)
										   .then();
		
		response.statusCode(Response.Status.OK.getStatusCode());
		
		log.info("Got response body: {}", response.extract().body().asString());
		
		ObjectId returned = response.extract().body().as(ObjectId.class);
		
		log.info("Got object id back from create request: {}", returned);
		return returned;
	}
	
	private InventoryItem update(User user, ObjectNode updateData, ObjectId id) throws JsonProcessingException {
		ValidatableResponse response = setupJwtCall(given(),  user.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
										   .contentType(ContentType.JSON)
										   .body(objectMapper.writeValueAsString(updateData))
										   .when()
										   .put(DEFAULT_TEST_DB_NAME, id.toHexString())
										   .then();
		
		response.statusCode(Response.Status.OK.getStatusCode());
		
		InventoryItem returned = response.extract().body().as(InventoryItem.class);
		
		log.info("Got object id back from create request: {}", returned);
		return returned;
	}
	
	public static Stream<Arguments> getSimpleAmountItems() {
		return Stream.of(
			Arguments.of(
				new SimpleAmountItem().setName(FAKER.commerce().productName())
			),
			Arguments.of(
				new SimpleAmountItem() {{
					this.getStorageMap().put(ObjectId.get(), new SingleAmountStoredWrapper(new AmountStored(0, this.getUnit())));
				}}.setName(FAKER.commerce().productName())
			)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getSimpleAmountItems")
	public void testCreateSimpleAmountItem(SimpleAmountItem item) throws JsonProcessingException {
		User user = this.getTestUserService().getTestUser();
		
		ObjectId returned = create(user, item);
		
		InventoryItem stored = inventoryItemService.get(DEFAULT_TEST_DB_NAME, returned);
		assertNotNull(stored);
		
		item.setId(returned);
		
		assertEquals(item, stored);
		
		//TODO:: check history
	}
	
	@Test
	public void testCreateListAmountItem() throws JsonProcessingException {
		User user = this.getTestUserService().getTestUser();
		ListAmountItem item = (ListAmountItem) new ListAmountItem().setName(FAKER.commerce().productName());
		ObjectId returned = create(user, item);
		
		InventoryItem stored = inventoryItemService.get(DEFAULT_TEST_DB_NAME, returned);
		assertNotNull(stored);
		
		item.setId(returned);
		
		assertEquals(item, stored);
		
		//TODO:: check history
	}
	
	@Test
	public void testCreateTrackedItem() throws JsonProcessingException {
		User user = this.getTestUserService().getTestUser();
		TrackedItem item = (TrackedItem) new TrackedItem()
											 .setTrackedItemIdentifierName("id")
											 .setName(FAKER.commerce().productName());
		ObjectId returned = create(user, item);
		
		InventoryItem stored = inventoryItemService.get(DEFAULT_TEST_DB_NAME, returned);
		assertNotNull(stored);
		
		item.setId(returned);
		
		assertEquals(item, stored);
		
		//TODO:: check history
	}
	
	@Test
	public void testUpdateTrackedItem() throws JsonProcessingException {
		User user = this.getTestUserService().getTestUser();
		TrackedItem item = (TrackedItem) new TrackedItem()
											 .setTrackedItemIdentifierName("id")
											 .setName(FAKER.commerce().productName());
		item.add(ObjectId.get(), new TrackedStored("1"));
		ObjectId returned = create(user, item);
		
		ObjectNode updateData = ObjectUtils.OBJECT_MAPPER.createObjectNode();
		updateData.put("name", FAKER.commerce().productName());
	}
	
	@Test
	public void testAddFromCsv() throws IOException {
		User user = this.getTestUserService().getTestUser();
		
		String csvData = this.itemsCsv.render();
		
		csvData += System.lineSeparator() + "Test Simple, testing a simple amount item, AMOUNT_SIMPLE,,1.00,,,";
		csvData += System.lineSeparator() + "Test List, testing a list amount item, AMOUNT_LIST,,1.00,,,";
		csvData += System.lineSeparator() + "Test Tracked, testing a tracked item, TRACKED,,1.00,,serial,";
		
		
		ImportBundleFileBody body = new ImportBundleFileBody();
		body.file = new ByteArrayInputStream(csvData.getBytes());
		body.fileName = "test.csv";
		
		
		ValidatableResponse response = setupJwtCall(given(),  user.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
										   .contentType(ContentType.MULTIPART)
										   .multiPart("file", csvData)
										   .multiPart("fileName", "test.csv")
										   .when()
										   .post("", DEFAULT_TEST_DB_NAME)
										   .then();
		
		response.statusCode(Response.Status.OK.getStatusCode());
		
		log.info("Got response body: {}", response.extract().body().asString());
	}
}