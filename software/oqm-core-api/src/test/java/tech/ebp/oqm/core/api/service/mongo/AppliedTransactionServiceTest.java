
package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.rest.search.AppliedTransactionSearch;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoObjectServiceTest;
import tech.units.indriya.unit.Units;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class AppliedTransactionServiceTest extends MongoObjectServiceTest<AppliedTransaction, AppliedTransactionService> {

	@Inject
	AppliedTransactionService appliedTransactionService;

	@Inject
	InventoryItemService inventoryItemService;

	@Inject
	InventoryItemTestObjectCreator itemTestObjectCreator;

	//TODO:: these default tests
	@Override
	protected AppliedTransaction getTestObject() {
		return null;//TODO
	}
//
//	@Test
//	public void injectTest() {
//		assertNotNull(appliedTransactionService);
//	}
//
//	@Test
//	public void listTest() {
//		this.defaultListTest(this.appliedTransactionService);
//	}
//
//	@Test
//	public void countTest() {
//		this.defaultCountTest(this.appliedTransactionService);
//	}
//
//	@Test
//	public void addTest() {
//		this.defaultAddTest(this.appliedTransactionService);
//	}
//
//	@Test
//	public void getObjectIdTest() {
//		this.defaultGetObjectIdTest(this.appliedTransactionService);
//	}
//
//	@Test
//	public void getStringTest() {
//		this.defaultGetStringTest(this.appliedTransactionService);
//	}
//
//	@Test
//	public void removeAllTest() {
//		this.defaultRemoveAllTest(this.appliedTransactionService);
//	}

//<editor-fold desc="Apply- Add Amount">
	//TODO:: Success- bulk - already present in block
	//TODO:: Success- bulk - not present in block
	//TODO:: Success- amount list- to new in list
	//TODO:: Success- amount list- to existing in list
	//TODO:: fail - any unique
	//TODO:: fail - any unique
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Add Whole">
	//TODO:: Success - bulk
	//TODO:: Success - amt list
	//TODO:: Success - unique list
	//TODO:: Success - unique single
	//TODO:: Fail - Unique single
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Checkin Full">
	//TODO:: Success - amount - to stored (existing)
	//TODO:: Success - amount - to stored (not existing)
	//TODO:: Success - amount - to block
	//TODO:: Fail - amount - No 'to' given
	//TODO:: Fail - amount - 'to block' given for list
	//TODO:: Success - whole - to block
	//TODO:: fail - whole - no to block given
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Checkin Part">
	//TODO:: When we have this implemented
//</editor-fold>
//<editor-fold desc="Apply- Checkout Amount">
	//TODO:: Success - bulk - from block
	//TODO:: Success - bulk - from stored
	//TODO:: fail - bulk - no 'from' given
	//TODO:: Success - amt list - from stored
	//TODO:: fail - amt list - from block/ not from stored
	//TODO:: fail - Unique given
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Checkout Whole">
	//TODO:: Success - bulk
	//TODO:: Success - amt list
	//TODO:: Success - unique list
	//TODO:: Success - unique single
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Subtract Amount">
	//TODO:: Success - bulk
	//TODO:: Success - amount list
	//TODO:: fail - less than 0 left in stored
	//TODO:: Fail - Unique (any)
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Subtract Whole">
	//TODO:: Success - bulk
	//TODO:: Success - amt list
	//TODO:: Success - unique list
	//TODO:: Success - unique single
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Transfer Amount">
	//TODO:: Success - bulk - to existing
	//TODO:: Success - bulk - to not existing
	//TODO:: Success - amt list - to stored given
	//TODO:: Success - amt list - no to stored given
	//TODO:: fail - unique anything
	//TODO:: fail - less than 0 in from stored
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Transfer Whole">
	//TODO:: Success - bulk
	//TODO:: Success - amt list
	//TODO:: Success - unique list
	//TODO:: Success - unique single
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
}