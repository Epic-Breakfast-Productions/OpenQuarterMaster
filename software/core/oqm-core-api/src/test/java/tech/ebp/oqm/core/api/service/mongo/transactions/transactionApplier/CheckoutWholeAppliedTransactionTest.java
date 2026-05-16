package tech.ebp.oqm.core.api.service.mongo.transactions.transactionApplier;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.history.events.DeleteEvent;
import tech.ebp.oqm.core.api.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckoutDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemAmountCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemWholeCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutForOqmEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutWholeTransaction;
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
public class CheckoutWholeAppliedTransactionTest extends AppliedTransactionServiceTest {
	
	@Test
	public void applyCheckoutWholeSuccessBulk() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		AmountStored initialStored = AmountStored.builder()
										 .item(item.getId())
										 .storageBlock(blockId)
										 .amount(Quantities.getQuantity(6, item.getUnit()))
										 .build();
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutWholeTransaction.builder()
														.toCheckout(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 0);
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(initialStoredId));
		assertFalse(storedHistory.isEmpty());
		DeleteEvent event = (DeleteEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemWholeCheckout resultingCheckout = (ItemWholeCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(details, resultingCheckout.getCheckoutDetails());
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckOutTransaction());
		assertEquals(initialStored, resultingCheckout.getCheckedOut());
	}
	
	@Test
	public void applyCheckoutWholeSuccessAmtList() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		AmountStored initialStored = AmountStored.builder()
										 .item(item.getId())
										 .storageBlock(blockId)
										 .amount(Quantities.getQuantity(6, item.getUnit()))
										 .build();
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutWholeTransaction.builder()
														.toCheckout(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 0);
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(initialStoredId));
		assertFalse(storedHistory.isEmpty());
		DeleteEvent event = (DeleteEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemWholeCheckout resultingCheckout = (ItemWholeCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(details, resultingCheckout.getCheckoutDetails());
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckOutTransaction());
		assertEquals(initialStored, resultingCheckout.getCheckedOut());
	}
	
	@Test
	public void applyCheckoutWholeSuccessUniqueMulti() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		UniqueStored initialStored = UniqueStored.builder()
										 .item(item.getId())
										 .storageBlock(blockId)
										 .build();
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutWholeTransaction.builder()
														.toCheckout(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 0);
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(initialStoredId));
		assertFalse(storedHistory.isEmpty());
		DeleteEvent event = (DeleteEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemWholeCheckout resultingCheckout = (ItemWholeCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(details, resultingCheckout.getCheckoutDetails());
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckOutTransaction());
		assertEquals(initialStored, resultingCheckout.getCheckedOut());
	}
	
	@Test
	public void applyCheckoutWholeSuccessUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		UniqueStored initialStored = UniqueStored.builder()
										 .item(item.getId())
										 .storageBlock(blockId)
										 .build();
		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		).getId();
		
		CheckoutDetails details = CheckoutDetails.builder()
									  .checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build())
									  .build();
		ItemStoredTransaction preApplyTransaction = CheckoutWholeTransaction.builder()
														.toCheckout(initialStoredId)
														.checkoutDetails(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(0, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 0);
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(initialStoredId));
		assertFalse(storedHistory.isEmpty());
		DeleteEvent event = (DeleteEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemWholeCheckout resultingCheckout = (ItemWholeCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(details, resultingCheckout.getCheckoutDetails());
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckOutTransaction());
		assertEquals(initialStored, resultingCheckout.getCheckedOut());
	}
	
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
}
