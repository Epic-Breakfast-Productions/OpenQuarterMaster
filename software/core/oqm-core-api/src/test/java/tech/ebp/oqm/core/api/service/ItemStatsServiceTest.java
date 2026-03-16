package tech.ebp.oqm.core.api.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.mongo.transactions.AppliedTransactionService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

@Slf4j
@QuarkusTest
public class ItemStatsServiceTest extends RunningServerTest {
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@Inject
	StoredService storedService;
	
	@Inject
	AppliedTransactionService appliedTransactionService;
	
	@Inject
	ItemStatsService itemStatsService;
	
	//TODO:: test Initial add stats
	//TODO:: test post update stats
	//TODO:: test post transaction stats
}