package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.QuarkusTestResource;
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
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.TotalPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.unit.PricePerUnit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.StorageBlockTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Tag("integration")
@Slf4j
@QuarkusTest
class InventoryItemsStatsTest extends RunningServerTest {
	
	@Inject
	InventoryItemTestObjectCreator testObjectCreator;
	@Inject
	StorageBlockTestObjectCreator testBlockCreator;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@Test
	public void testPricingTotals() throws JsonProcessingException {
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
		
		String finalItemStr = setupJwtCall(given(), testUser.getAttributes().get(TestUserService.TEST_JWT_ATT_KEY))
								  .body(
									  OBJECT_MAPPER.writeValueAsString(
										  AddWholeTransaction.builder()
											  .toAdd(stored)
											  .toBlock(block.getId())
											  .build()
									  )
								  )
								  .contentType(ContentType.JSON)
								  .get("/api/v1/db/" + DEFAULT_TEST_DB_NAME + "/inventory/item/"+item.getId())
								  .then().statusCode(200)
								  .extract().body().asString();
		item =
			OBJECT_MAPPER.readValue(
				finalItemStr,
				InventoryItem.class
			);
		
		log.info("Item json: {}", finalItemStr);
		log.info("Item: {}", item);
		
		{//verify item stats
			LinkedHashSet<TotalPricing> prices = item.getStats().getPrices();
			
			assertEquals(5, prices.size());
			
			prices.stream().forEach((tp)->assertNotNull(tp.getTotalPrice()));
			
			assertEquals(
				Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(6).create(),
				prices.stream().map(TotalPricing::getTotalPrice).reduce(MonetaryAmount::add).get()
			);
			
			assertEquals(
				Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create(),
				prices.stream().filter((p)->{return p.getLabel().equals("fromDefaultFlat");}).findFirst().get().getTotalPrice()
			);
			assertEquals(
				Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(2).create(),
				prices.stream().filter((p)->{return p.getLabel().equals("fromDefaultFlatOverridden");}).findFirst().get().getTotalPrice()
			);
			assertEquals(
				Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create(),
				prices.stream().filter((p)->{return p.getLabel().equals("fromDefaultPerAmount");}).findFirst().get().getTotalPrice()
			);
			
			assertEquals(
				Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create(),
				prices.stream().filter((p)->{return p.getLabel().equals("fromStoredFlat");}).findFirst().get().getTotalPrice()
			);
			assertEquals(
				Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create(),
				prices.stream().filter((p)->{return p.getLabel().equals("fromStoredPerAmount");}).findFirst().get().getTotalPrice()
			);
			
		}
		{//verify stored stats
			LinkedHashSet<TotalPricing> prices = item.getStats().getStorageBlockStats().get(block.getId()).getPrices();
			
			assertEquals(5, prices.size());
		}
		
		//TODO:: verify pricing in item stats
	}
	
}