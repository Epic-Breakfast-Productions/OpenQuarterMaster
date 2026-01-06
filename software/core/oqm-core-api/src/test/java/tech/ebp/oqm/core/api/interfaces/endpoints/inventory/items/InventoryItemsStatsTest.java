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
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.StorageBlockTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.LinkedHashSet;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Tag("integration")
@Slf4j
@QuarkusTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class)
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
											.label("fromDefault")
											.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
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
		
	}
	
}