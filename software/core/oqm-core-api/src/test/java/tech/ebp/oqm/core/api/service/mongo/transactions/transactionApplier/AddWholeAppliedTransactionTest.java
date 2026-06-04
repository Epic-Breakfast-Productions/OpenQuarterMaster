package tech.ebp.oqm.core.api.service.mongo.transactions.transactionApplier;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.history.events.CreateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
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
public class AddWholeAppliedTransactionTest extends AppliedTransactionServiceTest {

	@Test
	public void applyAddWholeSuccessAmtList() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
														.toAdd(
															AmountStored.builder()
																.item(item.getId())
																.amount(Quantities.getQuantity(5, item.getUnit()))
																.state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst().getStorageBlock()).build())
																.build()
														)
														.toBlock(item.getStorageBlocks().getFirst().getStorageBlock())
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
		CreateEvent event = (CreateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddWholeSuccessUniqueMulti() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
														.toAdd(
															UniqueStored.builder()
																.item(item.getId())
																.state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst().getStorageBlock()).build())
																.build()
														)
														.toBlock(item.getStorageBlocks().getFirst().getStorageBlock())
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
		UniqueStored storedFromSearch = (UniqueStored) storedSearchResult.getResults().getFirst();

		UniqueStored stored = (UniqueStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		CreateEvent event = (CreateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddWholeSuccessUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
														.toAdd(
															UniqueStored.builder()
																.item(item.getId())
																.state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst().getStorageBlock()).build())
																.build()
														)
														.toBlock(item.getStorageBlocks().getFirst().getStorageBlock())
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
		UniqueStored storedFromSearch = (UniqueStored) storedSearchResult.getResults().getFirst();

		UniqueStored stored = (UniqueStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		CreateEvent event = (CreateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddWholeFailBulk() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
														.toAdd(
															AmountStored.builder()
																.item(item.getId())
																.amount(Quantities.getQuantity(5, item.getUnit()))
																.state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst().getStorageBlock()).build())
																.build()
														)
														.toBlock(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add whole item to a bulk storage typed item.", e.getMessage());
	}

	@Test
	public void applyAddWholeFailAnotherUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
														.toAdd(
															UniqueStored.builder()
																.item(item.getId())
																.state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst().getStorageBlock()).build())
																.build()
														)
														.toBlock(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		ValidationException e = assertThrows(ValidationException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot store more than one globally unique stored item.", e.getMessage());
	}

	@Test
	public void applyAddWholeFailMismatchedBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
														.toAdd(
															UniqueStored.builder()
																.item(item.getId())
																.state(StoredInBlock.builder().storageBlock(ObjectId.get()).build())
																.build()
														)
														.toBlock(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Block given does not match block marked in stored.", e.getMessage());

		ItemStoredTransaction preApplyTransaction2 = AddWholeTransaction.builder()
														 .toAdd(
															 UniqueStored.builder()
																 .item(item.getId())
																 .state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst().getStorageBlock()).build())
																 .build()
														 )
														 .toBlock(new ObjectId())
														 .build();

		e = assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction2, entity));
		assertEquals("To Block given does not match block marked in stored.", e.getMessage());
	}

	@Test
	public void applyAddWholeFailBlockNotInItem() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
														.toAdd(
															UniqueStored.builder()
																.item(item.getId())
																.state(StoredInBlock.builder().storageBlock(ObjectId.get()).build())
																.build()
														)
														.toBlock(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Block given does not match block marked in stored.", e.getMessage());
	}

	@Test
	public void applyAddWholeFailItemMismatched() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
														.toAdd(
															UniqueStored.builder()
																.item(new ObjectId())
																.state(StoredInBlock.builder().storageBlock(item.getStorageBlocks().getFirst().getStorageBlock()).build())
																.build()
														)
														.toBlock(item.getStorageBlocks().getFirst().getStorageBlock())
														.build();

		IllegalArgumentException
			e =
			assertThrows(IllegalArgumentException.class, ()->this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored given is not associated with item.", e.getMessage());
	}

	//TODO:: any more?
}
