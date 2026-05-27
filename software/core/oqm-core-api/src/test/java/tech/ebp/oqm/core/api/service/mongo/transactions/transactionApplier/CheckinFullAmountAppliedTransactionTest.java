package tech.ebp.oqm.core.api.service.mongo.transactions.transactionApplier;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.history.events.CreateEvent;
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
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
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
public class CheckinFullAmountAppliedTransactionTest extends AppliedTransactionServiceTest {
	@Test
	public void applyCheckinFullAmountSuccessBulkToBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		AmountStored initialStored = AmountStored.builder()
										 .item(item.getId())
										 .state(StoredInBlock.builder().storageBlock(blockId).build())
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(
			DEFAULT_TEST_DB_NAME, null, item,
			CheckoutAmountTransaction.builder()
				.fromStored(initialStored.getId())
				.amount(Quantities.getQuantity(4, item.getUnit()))
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity
		);
		ObjectId checkoutId =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(appliedTransaction.getId()))
				.getResults()
				.getFirst()
				.getId();
		//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);
		
		ReturnFullCheckinDetails
			details =
			ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
		ItemStoredTransaction preApplyTransaction = CheckinFullTransaction.builder()
														.checkoutId(checkoutId)
														.toBlock(blockId)
														.details(details)
														.build();
		
		appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		
		SearchResult<ItemCheckout>
			checkoutSearch =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemAmountCheckout resultingCheckout = (ItemAmountCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckInTransaction());
		assertEquals(details, resultingCheckout.getCheckInDetails());
	}
	
	@Test
	public void applyCheckinFullAmountSuccessBulkToBlockWithoutStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId origBlockId = item.getStorageBlocks().getFirst();
		ObjectId newBlockId = this.storageBlockService.add(DEFAULT_TEST_DB_NAME, StorageBlock.builder().label(FAKER.location().building()).build(), entity).getId();
		item.getStorageBlocks().add(newBlockId);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);
		
		AmountStored initialStored = AmountStored.builder()
										 .item(item.getId())
										 .state(StoredInBlock.builder().storageBlock(origBlockId).build())
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);
		
		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(
			DEFAULT_TEST_DB_NAME, null, item,
			CheckoutAmountTransaction.builder()
				.fromStored(initialStored.getId())
				.amount(Quantities.getQuantity(4, item.getUnit()))
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity
		).getId();
		ObjectId
			checkoutId =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId)).getResults().getFirst().getId();
		//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);
		
		ReturnFullCheckinDetails
			details =
			ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
		ItemStoredTransaction preApplyTransaction = CheckinFullTransaction.builder()
														.checkoutId(checkoutId)
														.toBlock(newBlockId)
														.details(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(2, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 2);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(4, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		
		SearchResult<ItemCheckout>
			checkoutSearch =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemAmountCheckout resultingCheckout = (ItemAmountCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckInTransaction());
		assertEquals(details, resultingCheckout.getCheckInDetails());
	}
	
	@Test
	public void applyCheckinFullAmountSuccessBulkToStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		AmountStored initialStored = AmountStored.builder()
										 .item(item.getId())
										 .state(StoredInBlock.builder().storageBlock(blockId).build())
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);
		
		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(
			DEFAULT_TEST_DB_NAME, null, item,
			CheckoutAmountTransaction.builder()
				.fromStored(initialStored.getId())
				.amount(Quantities.getQuantity(4, item.getUnit()))
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity
		).getId();
		ObjectId
			checkoutId =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId)).getResults().getFirst().getId();
		//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);
		
		ReturnFullCheckinDetails
			details =
			ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
		ItemStoredTransaction preApplyTransaction = CheckinFullTransaction.builder()
														.checkoutId(checkoutId)
														.toStored(initialStored.getId())
														.details(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		
		SearchResult<ItemCheckout>
			checkoutSearch =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemAmountCheckout resultingCheckout = (ItemAmountCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckInTransaction());
		assertEquals(details, resultingCheckout.getCheckInDetails());
	}
	
	@Test
	public void applyCheckinFullAmountSuccessAmtListToStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		AmountStored initialStored = AmountStored.builder()
										 .item(item.getId())
										 .state(StoredInBlock.builder().storageBlock(blockId).build())
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);
		
		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(
			DEFAULT_TEST_DB_NAME, null, item,
			CheckoutAmountTransaction.builder()
				.fromStored(initialStored.getId())
				.amount(Quantities.getQuantity(4, item.getUnit()))
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity
		).getId();
		ObjectId
			checkoutId =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId)).getResults().getFirst().getId();
		//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);
		
		ReturnFullCheckinDetails details =
			ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
		ItemStoredTransaction preApplyTransaction = CheckinFullTransaction.builder()
														.checkoutId(checkoutId)
														.toStored(initialStored.getId())
														.details(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		
		SearchResult<ItemCheckout>
			checkoutSearch =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemAmountCheckout resultingCheckout = (ItemAmountCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckInTransaction());
		assertEquals(details, resultingCheckout.getCheckInDetails());
	}
	
	@Test
	public void applyCheckinFullAmountSuccessAmtListToBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();
		
		AmountStored initialStored = AmountStored.builder()
										 .item(item.getId())
										 .state(StoredInBlock.builder().storageBlock(blockId).build())
										 .amount(Quantities.getQuantity(5, item.getUnit()))
										 .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);
		
		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(
			DEFAULT_TEST_DB_NAME, null, item,
			CheckoutAmountTransaction.builder()
				.fromStored(initialStored.getId())
				.amount(Quantities.getQuantity(4, item.getUnit()))
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity
		).getId();
		ObjectId
			checkoutId =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId)).getResults().getFirst().getId();
		//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);
		
		ReturnFullCheckinDetails
			details =
			ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
		ItemStoredTransaction preApplyTransaction = CheckinFullTransaction.builder()
														.checkoutId(checkoutId)
														.toBlock(blockId)
														.details(details)
														.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
		
		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));
		
		assertEquals(2, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats
		
		
		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 2);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();
		
		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(4, item.getUnit()), stored.getAmount());
		
		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		
		
		SearchResult<ItemCheckout>
			checkoutSearch =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemAmountCheckout resultingCheckout = (ItemAmountCheckout) checkoutSearch.getResults().getFirst();
		
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckInTransaction());
		assertEquals(details, resultingCheckout.getCheckInDetails());
	}
	
	//TODO:: more?
}
