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
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageBlockSettings;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferWholeTransaction;
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
public class TransferWholeAppliedTransactionTest extends AppliedTransactionServiceTest {

	@Test
	public void applyTransferWholeSuccessBulk() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst().getStorageBlock();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(StorageBlockSettings.builder().storageBlock(secondBlock).build());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
									  .item(item.getId())
									  .state(StoredInBlock.builder().storageBlock(firstBlock).build())
									  .amount(Quantities.getQuantity(5, item.getUnit()))
									  .build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferWholeTransaction.builder()
														.fromBlock(firstBlock)
														.toBlock(secondBlock)
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
		AmountStored firstStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(0);

		{
			AmountStored firstStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);
			assertEquals(Quantities.getQuantity(5, item.getUnit()), firstStored.getAmount());

			SearchResult<ObjectHistoryEvent>
				storedHistory =
				this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}

	@Test
	public void applyTransferWholeSuccessAmtList() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst().getStorageBlock();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(StorageBlockSettings.builder().storageBlock(secondBlock).build());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
									  .item(item.getId())
									  .state(StoredInBlock.builder().storageBlock(firstBlock).build())
									  .amount(Quantities.getQuantity(5, item.getUnit()))
									  .build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferWholeTransaction.builder()
														.fromBlock(firstBlock)
														.storedToTransfer(initialStoredId)
														.toBlock(secondBlock)
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
		AmountStored firstStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(0);

		{
			AmountStored firstStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);
			assertEquals(Quantities.getQuantity(5, item.getUnit()), firstStored.getAmount());

			SearchResult<ObjectHistoryEvent>
				storedHistory =
				this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}

	@Test
	public void applyTransferWholeSuccessUniqueMulti() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst().getStorageBlock();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(StorageBlockSettings.builder().storageBlock(secondBlock).build());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, UniqueStored.builder()
									  .item(item.getId())
									  .state(StoredInBlock.builder().storageBlock(firstBlock).build())
									  .build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferWholeTransaction.builder()
														.fromBlock(firstBlock)
														.storedToTransfer(initialStoredId)
														.toBlock(secondBlock)
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
		UniqueStored firstStoredFromSearch = (UniqueStored) storedSearchResult.getResults().get(0);

		{
			UniqueStored firstStored = (UniqueStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);

			SearchResult<ObjectHistoryEvent>
				storedHistory =
				this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}


	@Test
	public void applyTransferWholeSuccessUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst().getStorageBlock();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(StorageBlockSettings.builder().storageBlock(secondBlock).build());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, UniqueStored.builder()
									  .item(item.getId())
									  .state(StoredInBlock.builder().storageBlock(firstBlock).build())
									  .build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferWholeTransaction.builder()
														.fromBlock(firstBlock)
														.storedToTransfer(initialStoredId)
														.toBlock(secondBlock)
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
		UniqueStored firstStoredFromSearch = (UniqueStored) storedSearchResult.getResults().get(0);

		{
			UniqueStored firstStored = (UniqueStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);

			SearchResult<ObjectHistoryEvent>
				storedHistory =
				this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}


	//TODO:: any more?
}
