package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.StoredPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.unit.PricePerUnit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.StorageBlockTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import javax.money.Monetary;
import java.util.LinkedHashSet;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Tag("integration")
@Slf4j
@QuarkusTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class)
//@TestHTTPEndpoint(StoredEndpoints.class)
public class StoredInItemEndpointsTest extends RunningServerTest {
	
	@Inject
	StorageBlockTestObjectCreator testBlockCreator;
	@Inject
	InventoryItemTestObjectCreator testObjectCreator;

	@Inject
	ObjectMapper objectMapper;
	
	@Test
	public void testSearchEmptyDb() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();
		
		String json = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
						  .body(objectMapper.writeValueAsString(testObjectCreator.getTestObject()))
						  .contentType(ContentType.JSON)
						  .post("/api/v1/db/"+DEFAULT_TEST_DB_NAME+"/inventory/item")
						  .then().statusCode(200)
						  .extract().body().asString();
		
		String id = OBJECT_MAPPER.readValue(json, InventoryItem.class).getId().toHexString();
		
		ValidatableResponse response = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
										   .when()
										   .get("/api/v1/db/"+DEFAULT_TEST_DB_NAME+"/inventory/item/"+id+"/stored")
										   .then()
										   .statusCode(200);
		
		log.info("Search result: {}", response.extract().asString());
	}
	
	@Test
	public void testSearchSimple() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();
		
		StorageBlock block =
			OBJECT_MAPPER.readValue(
				setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
					.body(OBJECT_MAPPER.writeValueAsString(testBlockCreator.getTestObject()))
					.contentType(ContentType.JSON)
					.post("/api/v1/db/" + DEFAULT_TEST_DB_NAME + "/inventory/storage-block")
					.then().statusCode(200)
					.extract().body().asString(),
				StorageBlock.class
			);
		
		
		InventoryItem item =
			OBJECT_MAPPER.readValue(
				setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
					.body(
						OBJECT_MAPPER.writeValueAsString(
							testObjectCreator.getTestObject()
								.setStorageType(StorageType.AMOUNT_LIST)
								.setStorageBlocks(new LinkedHashSet<>() {{
													  add(block.getId());
												  }}
								)
								.setDefaultPrices(new LinkedHashSet<>() {{
									add(StoredPricing.builder()
											.label("fromDefaultFlat")
											.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
											.build());
									add(StoredPricing.builder()
											.label("fromDefaultFlatOverridden")
											.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
											.build());
									add(StoredPricing.builder()
											.label("fromDefaultPerAmount")
											.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(0).create())
											.pricePerUnit(
												PricePerUnit.builder()
													.price(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
													.unit(OqmProvidedUnits.UNIT)
													.build()
											
											)
											.build());
								}})
						)
					)
					.contentType(ContentType.JSON)
					.post("/api/v1/db/" + DEFAULT_TEST_DB_NAME + "/inventory/item")
					.then().statusCode(200)
					.extract().body().asString(),
				InventoryItem.class
			);
		
		AmountStored stored = AmountStored.builder()
								  .amount(UnitUtils.Quantities.UNIT_ONE)
								  .item(item.getId())
								  .storageBlock(block.getId())
								  .prices(new LinkedHashSet<>() {{
									  add(StoredPricing.builder()
											  .label("fromStoredFlat")
											  .flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
											  .build());
									  add(StoredPricing.builder()
											  .label("fromDefaultFlatOverridden")
											  .flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(2).create())
											  .build());
									  add(StoredPricing.builder()
											  .label("fromStoredPerAmount")
											  .flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(0).create())
											  .pricePerUnit(
												  PricePerUnit.builder()
													  .price(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
													  .unit(OqmProvidedUnits.UNIT)
													  .build()
											  
											  )
											  .build());
								  }})
								  .build();
		
		AppliedTransaction transaction =
			OBJECT_MAPPER.readValue(
				setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
					.body(
						OBJECT_MAPPER.writeValueAsString(
							AddWholeTransaction.builder()
								.toAdd(stored)
								.toBlock(block.getId())
								.build()
						)
					)
					.contentType(ContentType.JSON)
					.post("/api/v1/db/" + DEFAULT_TEST_DB_NAME + "/inventory/item/"+item.getId()+"/stored/transaction")
					.then().statusCode(200)
					.extract().body().asString(),
				AppliedTransaction.class
			);
		
		
		ValidatableResponse response = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
										   .when()
										   .get("/api/v1/db/"+DEFAULT_TEST_DB_NAME+"/inventory/item/"+item.getId()+"/stored")
										   .then()
										   .statusCode(200);
		
		log.info("Search result: {}", response.extract().asString());
		
		SearchResult<AmountStored> result = OBJECT_MAPPER.readValue(response.extract().asString(), new TypeReference<>() {});
		
		assertEquals(1, result.getNumResults());
		
		AmountStored storedResult = (AmountStored) result.getResults().getFirst();
		//		assertNotNull(storedResult.getCalculatedPrices());
	}
	
	@Test
	public void testGetSimple() throws JsonProcessingException {
		User testUser = this.getTestUserService().getTestUser();
		
		StorageBlock block =
			OBJECT_MAPPER.readValue(
				setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
					.body(OBJECT_MAPPER.writeValueAsString(testBlockCreator.getTestObject()))
					.contentType(ContentType.JSON)
					.post("/api/v1/db/" + DEFAULT_TEST_DB_NAME + "/inventory/storage-block")
					.then().statusCode(200)
					.extract().body().asString(),
				StorageBlock.class
			);
		
		
		InventoryItem item =
			OBJECT_MAPPER.readValue(
				setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
					.body(
						OBJECT_MAPPER.writeValueAsString(
							testObjectCreator.getTestObject()
								.setStorageType(StorageType.AMOUNT_LIST)
								.setStorageBlocks(new LinkedHashSet<>() {{
													  add(block.getId());
												  }}
								)
								.setDefaultPrices(new LinkedHashSet<>() {{
									add(StoredPricing.builder()
											.label("fromDefaultFlat")
											.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
											.build());
									add(StoredPricing.builder()
											.label("fromDefaultFlatOverridden")
											.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
											.build());
									add(StoredPricing.builder()
											.label("fromDefaultPerAmount")
											.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(0).create())
											.pricePerUnit(
												PricePerUnit.builder()
													.price(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
													.unit(OqmProvidedUnits.UNIT)
													.build()
											
											)
											.build());
								}})
						)
					)
					.contentType(ContentType.JSON)
					.post("/api/v1/db/" + DEFAULT_TEST_DB_NAME + "/inventory/item")
					.then().statusCode(200)
					.extract().body().asString(),
				InventoryItem.class
			);
		
		AmountStored stored = AmountStored.builder()
								  .amount(UnitUtils.Quantities.UNIT_ONE)
								  .item(item.getId())
								  .storageBlock(block.getId())
								  .prices(new LinkedHashSet<>() {{
									  add(StoredPricing.builder()
											  .label("fromStoredFlat")
											  .flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
											  .build());
									  add(StoredPricing.builder()
											  .label("fromDefaultFlatOverridden")
											  .flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(2).create())
											  .build());
									  add(StoredPricing.builder()
											  .label("fromStoredPerAmount")
											  .flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(0).create())
											  .pricePerUnit(
												  PricePerUnit.builder()
													  .price(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
													  .unit(OqmProvidedUnits.UNIT)
													  .build()
											  
											  )
											  .build());
								  }})
								  .build();
		
		AppliedTransaction transaction =
			OBJECT_MAPPER.readValue(
				setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
					.body(
						OBJECT_MAPPER.writeValueAsString(
							AddWholeTransaction.builder()
								.toAdd(stored)
								.toBlock(block.getId())
								.build()
						)
					)
					.contentType(ContentType.JSON)
					.post("/api/v1/db/" + DEFAULT_TEST_DB_NAME + "/inventory/item/"+item.getId()+"/stored/transaction")
					.then().statusCode(200)
					.extract().body().asString(),
				AppliedTransaction.class
			);
		
		
		ValidatableResponse response = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
										   .when()
										   .get("/api/v1/db/"+DEFAULT_TEST_DB_NAME+"/inventory/item/"+item.getId()+"/stored/" + transaction.getAffectedStored().stream().findFirst().get().toHexString())
										   .then()
										   .statusCode(200);
		
		log.info("result: {}", response.extract().asString());
		
		AmountStored result = OBJECT_MAPPER.readValue(response.extract().asString(), AmountStored.class);
		//TODO:: assertions
//		assertNotNull(result.getCalculatedPrices());
	}
	
}
