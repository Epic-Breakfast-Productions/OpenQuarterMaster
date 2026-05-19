package tech.ebp.oqm.core.api.service.mongo.transactions.transactionApplier;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckoutDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemAmountCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.ReturnFullCheckinDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.checkedInBy.CheckedInByOqmEntity;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutForOqmEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinFullTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.ItemCheckoutSearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.units.indriya.quantity.Quantities;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.ebp.oqm.core.api.model.object.history.details.HistoryDetailType.ITEM_TRANSACTION;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
@QuarkusTestResource(value = KafkaCompanionResource.class, restrictToAnnotatedClass = true)
public class CheckoutAmountAppliedTransactionTest extends AppliedTransactionServiceTest {
	
	@Test
	public void applyCheckoutAmountSuccessBulkFromBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.amount(Quantities.getQuantity(6, item.getUnit()))
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(blockId)
														.checkoutDetails(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(1, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(1, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemAmountCheckout resultingCheckout = (ItemAmountCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(initialStoredId, resultingCheckout.getFromStored());
		assertEquals(details, resultingCheckout.getCheckoutDetails());
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckOutTransaction());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), resultingCheckout.getCheckedOut());
	}
	
	@Test
	public void applyCheckoutAmountSuccessBulkFromStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.amount(Quantities.getQuantity(6, item.getUnit()))
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromStored(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(1, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(1, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemAmountCheckout resultingCheckout = (ItemAmountCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(initialStoredId, resultingCheckout.getFromStored());
		assertEquals(details, resultingCheckout.getCheckoutDetails());
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckOutTransaction());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), resultingCheckout.getCheckedOut());
	}
	
	@Test
	public void applyCheckoutAmountSuccessAmtList() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.amount(Quantities.getQuantity(6, item.getUnit()))
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromStored(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(1, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(1, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemAmountCheckout resultingCheckout = (ItemAmountCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(initialStoredId, resultingCheckout.getFromStored());
		assertEquals(details, resultingCheckout.getCheckoutDetails());
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckOutTransaction());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), resultingCheckout.getCheckedOut());
	}
	
	@Test
	public void applyCheckoutAmountFailBulkFromNothing() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.amount(Quantities.getQuantity(6, item.getUnit()))
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.checkoutDetails(details)
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("No stored or block given to checkout from.", e.getMessage());
	}
	
	@Test
	public void applyCheckoutAmountFailAmtListFromNothing() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.amount(Quantities.getQuantity(6, item.getUnit()))
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.checkoutDetails(details)
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("No stored given to checkout from.", e.getMessage());
	}
	
	@Test
	public void applyCheckoutAmountFailAmtListFromNoStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.amount(Quantities.getQuantity(6, item.getUnit()))
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(blockId)
														.checkoutDetails(details)
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("No stored given to checkout from.", e.getMessage());
	}
	
	@Test
	public void applyCheckoutAmountFailAmtListBadBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.amount(Quantities.getQuantity(6, item.getUnit()))
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.fromBlock(new ObjectId())
														.fromStored(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("From Storage block given mismatched stored's block.", e.getMessage());
	}
	
	@Test
	public void applyCheckoutAmountFailBulkNotEnoughToCheckout() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.amount(Quantities.getQuantity(6, item.getUnit()))
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(7, item.getUnit()))
														.fromBlock(blockId)
														.fromStored(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Resulting amount less than zero. (subtracting 7 units from 6 units resulting in -1 units)", e.getMessage());
	}
	
	@Test
	public void applyCheckoutAmountFailAmtListNotEnoughToCheckout() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.amount(Quantities.getQuantity(6, item.getUnit()))
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(7, item.getUnit()))
														.fromBlock(blockId)
														.fromStored(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Resulting amount less than zero. (subtracting 7 units from 6 units resulting in -1 units)", e.getMessage());
	}
	
	@Test
	public void applyCheckoutAmountFailUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			UniqueStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(7, item.getUnit()))
														.fromBlock(blockId)
														.fromStored(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot checkout an amount from a unique type.", e.getMessage());
	}
	
	@Test
	public void applyCheckoutAmountFailUniqueMulti() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			UniqueStored.builder()
				.item(item.getId())
				.storageBlock(blockId)
				.build(),
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutAmountTransaction.builder()
														.amount(Quantities.getQuantity(7, item.getUnit()))
														.fromBlock(blockId)
														.fromStored(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot checkout an amount from a unique type.", e.getMessage());
	}
	
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
}
