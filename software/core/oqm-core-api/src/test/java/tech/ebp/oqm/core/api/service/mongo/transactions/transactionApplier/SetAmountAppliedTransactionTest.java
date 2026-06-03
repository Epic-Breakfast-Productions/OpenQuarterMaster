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
import tech.ebp.oqm.core.api.model.object.history.events.DeleteEvent;
import tech.ebp.oqm.core.api.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckoutDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemWholeCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutForOqmEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageBlockSettings;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set.SetAmountTransaction;
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
public class SetAmountAppliedTransactionTest extends AppliedTransactionServiceTest {
	@Test
	public void applySetAmountSuccessBulkNotInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.block(item.getStorageBlocks().getFirst().getStorageBlock())
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
	}


	@Test
	public void applySetAmountSuccessBulkAlreadyInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		AmountStored originalStored = AmountStored.builder()
										  .item(item.getId())
										  .state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst().getStorageBlock()).build())
										  .amount(Quantities.getQuantity(5, item.getUnit()))
										  .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);


		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
														.amount(Quantities.getQuantity(6, item.getUnit()))
														.block(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(6, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(originalStored.getId(), stored.getId());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(6, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}


	@Test
	public void applySetAmountSuccessAmtListExistingStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		AmountStored originalStored = AmountStored.builder()
										  .item(item.getId())
										  .state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst().getStorageBlock()).build())
										  .amount(Quantities.getQuantity(5, item.getUnit()))
										  .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);


		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
														.amount(Quantities.getQuantity(6, item.getUnit()))
														.block(item.getStorageBlocks().getFirst().getStorageBlock())
														.stored(originalStored.getId())
														.build();

		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(6, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(originalStored.getId(), stored.getId());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(6, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applySetAmountFailUniqueMulti() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.block(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add an amount to a unique item.", e.getMessage());
	}

	@Test
	public void applySetAmountFailUniqueSingle() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.block(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add an amount to a unique item.", e.getMessage());
	}

	@Test
	public void applySetAmountFailBulkBlockNotInInventory() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ObjectId badBlockId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.block(badBlockId)
														.build();

		ValidationException e = assertThrows(ValidationException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Storage block " + badBlockId + " not used to hold this item (" + item.getId() + ").", e.getMessage());
	}

	@Test
	public void applySetAmountFailBulkStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ObjectId badStoredId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.stored(badStoredId)
														.block(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored given does not match stored found in block.", e.getMessage());
	}

	@Test
	public void applySetAmountFailAmtListBadStored() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ObjectId badStoredId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.stored(badStoredId)
														.block(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Could not find Stored with id " + badStoredId, e.getMessage());
	}

	@Test
	public void applySetAmountFailAmtListBadStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ObjectId origBlock = item.getStorageBlocks().getFirst().getStorageBlock();
		ObjectId otherBlockId = this.storageBlockService.add(DEFAULT_TEST_DB_NAME, StorageBlock.builder().label(FAKER.location().building()).build(), entity).getId();

		item.getStorageBlocks().add(StorageBlockSettings.builder().storageBlock(otherBlockId).build());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		AmountStored originalStored = AmountStored.builder()
										  .item(item.getId())
										  .state(StoredInBlock.builder().storageBlock(origBlock).build())
										  .amount(Quantities.getQuantity(5, item.getUnit()))
										  .build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.stored(originalStored.getId())
														.block(otherBlockId)
														.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored given does not exist in block.", e.getMessage());
	}

	@Test
	public void applySetAmountFailAmtListNewEntry() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
														.amount(Quantities.getQuantity(5, item.getUnit()))
														.block(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Must specify a stored to set the amount of.", e.getMessage());
	}

	//TODO:: any more?
}
