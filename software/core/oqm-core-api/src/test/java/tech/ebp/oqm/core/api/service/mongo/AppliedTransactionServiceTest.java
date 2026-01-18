
package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.history.events.CreateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.DeleteEvent;
import tech.ebp.oqm.core.api.model.object.history.events.ReCreateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckoutDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemAmountCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemWholeCheckout;
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
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinFullTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set.SetAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.search.HistorySearch;
import tech.ebp.oqm.core.api.model.rest.search.ItemCheckoutSearch;
import tech.ebp.oqm.core.api.model.rest.search.StoredSearch;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.mongo.transactions.AppliedTransactionService;
import tech.ebp.oqm.core.api.service.notification.EventNotificationWrapper;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.StorageBlockTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoObjectServiceTest;
import tech.units.indriya.quantity.Quantities;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.model.object.history.details.HistoryDetailType.ITEM_TRANSACTION;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

/**
 * TODO:: rework to test each class individually, test nominally here
 */
@Slf4j
@QuarkusTest
@QuarkusTestResource(value = KafkaCompanionResource.class, restrictToAnnotatedClass = true)
class AppliedTransactionServiceTest extends MongoObjectServiceTest<AppliedTransaction, AppliedTransactionService> {

	@Inject
	AppliedTransactionService appliedTransactionService;

	@Inject
	StorageBlockService storageBlockService;

	@Inject
	StorageBlockTestObjectCreator storageBlockTestObjectCreator;

	@Inject
	InventoryItemService inventoryItemService;

	@Inject
	InventoryItemTestObjectCreator itemTestObjectCreator;

	@Inject
	StoredService storedService;

	@Inject
	ItemCheckoutService checkoutService;

	@InjectKafkaCompanion
	KafkaCompanion kafkaCompanion;

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


	private InventoryItem setupItem(StorageType storageType, InteractingEntity entity) {
		StorageBlock storageBlock = storageBlockTestObjectCreator.getTestObject();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, storageBlock, entity);
		InventoryItem item = this.itemTestObjectCreator.getTestObject().setStorageType(storageType);
		item.getStorageBlocks().add(storageBlock.getId());
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, entity);
		return item;
	}

	private void clearQueues() {
		this.kafkaCompanion
			.topics()
			.delete(HistoryEventNotificationService.ALL_EVENT_TOPIC)
//			.clear(HistoryEventNotificationService.ALL_EVENT_TOPIC)
		;
	}

	//TODO:: this with all following tests
	private List<EventNotificationWrapper> assertMessages(
		EventType... expectedEvents
	) throws JsonProcessingException {
		//TODO:: get list of all sent messages instead of predicted number
//		List<EventNotificationWrapper> eventWrappers = new ArrayList<>();
//		boolean awaiting = true;
//		do{
//			try {
//				eventWrappers.add(
//					ObjectUtils.OBJECT_MAPPER.readValue(
//						this.kafkaCompanion.consumeStrings()
//							.fromTopics(HistoryEventNotificationService.ALL_EVENT_TOPIC, 1)
//							.awaitCompletion(Duration.of(10, ChronoUnit.SECONDS))
//							.getFirstRecord().value(),
//
//
////						this.kafkaCompanion.consumeStrings()
////							.fromTopics(HistoryEventNotificationService.ALL_EVENT_TOPIC)
////							.awaitNextRecord(Duration.of(10, ChronoUnit.SECONDS))
//////							.stream().findFirst().get().value(),
//////							.getFirstRecord().value(),
//						EventNotificationWrapper.class
//					)
//				);
//				log.info("Got another wrapper");
//			} catch (AssertionError e){
//				awaiting = false;
//			}
//		}while(awaiting);


		ConsumerTask<String, String> messagesFromAll = this.kafkaCompanion.consumeStrings().fromTopics(
			HistoryEventNotificationService.ALL_EVENT_TOPIC
			, expectedEvents.length
		);
		messagesFromAll.awaitCompletion();
		assertEquals(expectedEvents.length, messagesFromAll.count());

		List<EventNotificationWrapper> eventWrappers = messagesFromAll.stream()
			.map(record-> {
				try {
					return ObjectUtils.OBJECT_MAPPER.readValue(record.value(), EventNotificationWrapper.class);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			})
			.collect(Collectors.toList());
		log.info("Found messages for transaction: {}", eventWrappers);

		assertEquals(expectedEvents.length, eventWrappers.size());

		for (int i = 0; i < expectedEvents.length; i++) {
			EventNotificationWrapper curWrapper = eventWrappers.get(i);
			EventType expectedType = expectedEvents[i];

			assertEquals(expectedType, curWrapper.getEvent().getType());
		}

		return eventWrappers;
	}


	//<editor-fold desc="Apply- Add Amount">
	@Test
	public void applyAddAmountSuccessBulkNotInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		this.clearQueues();
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));


		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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

		List<EventNotificationWrapper> messages = this.assertMessages(EventType.CREATE, EventType.UPDATE);
		//TODO:: verify messages
	}

	@Test
	public void applyAddAmountSuccessBulkAlreadyInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		AmountStored originalStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);


		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toBlock(item.getStorageBlocks().getFirst())
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(10, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(originalStored.getId(), stored.getId());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(10, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddAmountSuccessAmtListNewEntry() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toBlock(item.getStorageBlocks().getFirst())
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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
	public void applyAddAmountSuccessAmtListExistingStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		AmountStored originalStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);


		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toBlock(item.getStorageBlocks().getFirst())
			.toStored(originalStored.getId())
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(10, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(originalStored.getId(), stored.getId());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(10, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applyAddAmountFailUniqueMulti() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add an amount to a unique item.", e.getMessage());
	}

	@Test
	public void applyAddAmountFailUniqueSingle() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add an amount to a unique item.", e.getMessage());
	}

	@Test
	public void applyAddAmountFailBulkBlockNotInInventory() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ObjectId badBlockId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toBlock(badBlockId)
			.build();

		ValidationException e = assertThrows(ValidationException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Storage block " + badBlockId + " not used to hold this item ("+item.getId().toHexString()+").", e.getMessage());
	}

	@Test
	public void applyAddAmountFailBulkStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ObjectId badStoredId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toStored(badStoredId)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored given does not match stored found in block.", e.getMessage());
	}

	@Test
	public void applyAddAmountFailAmtListBadStored() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ObjectId badStoredId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toStored(badStoredId)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Could not find Stored with id " + badStoredId, e.getMessage());
	}

	@Test
	public void applyAddAmountFailAmtListBadStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ObjectId origBlock = item.getStorageBlocks().getFirst();
		ObjectId otherBlockId = this.storageBlockService.add(DEFAULT_TEST_DB_NAME, StorageBlock.builder().label(FAKER.location().building()).build(), entity).getId();

		item.getStorageBlocks().add(otherBlockId);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		AmountStored originalStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(origBlock)
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);

		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.toStored(originalStored.getId())
			.toBlock(otherBlockId)
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored given does not exist in block.", e.getMessage());
	}

	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Add Whole">

	@Test
	public void applyAddWholeSuccessAmtList() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
			.toAdd(
				AmountStored.builder()
					.item(item.getId())
					.amount(Quantities.getQuantity(5, item.getUnit()))
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		ValidationException e = assertThrows(ValidationException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add more than one stored held for type UNIQUE_SINGLE", e.getMessage());
	}

	@Test
	public void applyAddWholeFailMismatchedBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = AddWholeTransaction.builder()
			.toAdd(
				UniqueStored.builder()
					.item(item.getId())
					.storageBlock(new ObjectId())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Block given does not match block marked in stored.", e.getMessage());

		ItemStoredTransaction preApplyTransaction2 = AddWholeTransaction.builder()
			.toAdd(
				UniqueStored.builder()
					.item(item.getId())
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(new ObjectId())
			.build();

		e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction2, entity));
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
					.storageBlock(new ObjectId())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
					.storageBlock(item.getStorageBlocks().getFirst())
					.build()
			)
			.toBlock(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored given is not associated with item.", e.getMessage());
	}

	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Checkin Full">
	@Test
	public void applyCheckinFullAmountSuccessBulkToBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();

		AmountStored initialStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(blockId)
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item,
			CheckoutAmountTransaction.builder()
				.fromStored(initialStored.getId())
				.amount(Quantities.getQuantity(4, item.getUnit()))
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity);
		ObjectId checkoutId =
			this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(appliedTransaction.getId().toHexString())).getResults().getFirst().getId();
//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		ReturnFullCheckinDetails details = ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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


		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId().toHexString()));
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
			.storageBlock(origBlockId)
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);

		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item,
			CheckoutAmountTransaction.builder()
				.fromStored(initialStored.getId())
				.amount(Quantities.getQuantity(4, item.getUnit()))
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity).getId();
		ObjectId checkoutId = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId.toHexString())).getResults().getFirst().getId();
//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		ReturnFullCheckinDetails details = ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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


		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId().toHexString()));
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
			.storageBlock(blockId)
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);

		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item,
			CheckoutAmountTransaction.builder()
				.fromStored(initialStored.getId())
				.amount(Quantities.getQuantity(4, item.getUnit()))
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity).getId();
		ObjectId checkoutId = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId.toHexString())).getResults().getFirst().getId();
//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		ReturnFullCheckinDetails details = ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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


		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId().toHexString()));
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
			.storageBlock(blockId)
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);

		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item,
			CheckoutAmountTransaction.builder()
				.fromStored(initialStored.getId())
				.amount(Quantities.getQuantity(4, item.getUnit()))
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity).getId();
		ObjectId checkoutId = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId.toHexString())).getResults().getFirst().getId();
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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


		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId().toHexString()));
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
			.storageBlock(blockId)
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);

		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item,
			CheckoutAmountTransaction.builder()
				.fromStored(initialStored.getId())
				.amount(Quantities.getQuantity(4, item.getUnit()))
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity).getId();
		ObjectId checkoutId = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId.toHexString())).getResults().getFirst().getId();
//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		ReturnFullCheckinDetails details = ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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


		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId().toHexString()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemAmountCheckout resultingCheckout = (ItemAmountCheckout) checkoutSearch.getResults().getFirst();

		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckInTransaction());
		assertEquals(details, resultingCheckout.getCheckInDetails());
	}

	@Test
	public void applyCheckinFullWholeSuccessBulk() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();

		AmountStored initialStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(blockId)
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);

		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item,
			CheckoutWholeTransaction.builder()
				.toCheckout(initialStored.getId())
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity).getId();
		ObjectId checkoutId = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId.toHexString())).getResults().getFirst().getId();
//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		ReturnFullCheckinDetails details = ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
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

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(5, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		ReCreateEvent event = (ReCreateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());


		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId().toHexString()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemWholeCheckout resultingCheckout = (ItemWholeCheckout) checkoutSearch.getResults().getFirst();

		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckInTransaction());
		assertEquals(details, resultingCheckout.getCheckInDetails());
	}

	@Test
	public void applyCheckinFullWholeSuccessUniqueMulti() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();

		UniqueStored initialStored = UniqueStored.builder()
			.item(item.getId())
			.storageBlock(blockId)
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);

		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item,
			CheckoutWholeTransaction.builder()
				.toCheckout(initialStored.getId())
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity).getId();
		ObjectId checkoutId = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId.toHexString())).getResults().getFirst().getId();
//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		ReturnFullCheckinDetails details = ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
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

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(1, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		UniqueStored storedFromSearch = (UniqueStored) storedSearchResult.getResults().getFirst();

		UniqueStored stored = (UniqueStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		ReCreateEvent event = (ReCreateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());


		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId().toHexString()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemWholeCheckout resultingCheckout = (ItemWholeCheckout) checkoutSearch.getResults().getFirst();

		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckInTransaction());
		assertEquals(details, resultingCheckout.getCheckInDetails());
	}

	@Test
	public void applyCheckinFullWholeSuccessUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);
		ObjectId blockId = item.getStorageBlocks().getFirst();

		UniqueStored initialStored = UniqueStored.builder()
			.item(item.getId())
			.storageBlock(blockId)
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, initialStored, entity);

		ObjectId checkoutTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item,
			CheckoutWholeTransaction.builder()
				.toCheckout(initialStored.getId())
				.checkoutDetails(CheckoutDetails.builder().checkedOutFor(CheckoutForOqmEntity.builder().entity(entity.getId()).build()).build())
				.build(),
			entity).getId();
		ObjectId checkoutId = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setCheckOutTransaction(checkoutTransactionId.toHexString())).getResults().getFirst().getId();
//		AppliedTransaction checkoutTransactionId = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);

		ReturnFullCheckinDetails details = ReturnFullCheckinDetails.builder().notes(FAKER.lorem().paragraph()).checkedInBy(CheckedInByOqmEntity.builder().entity(entity.getId()).build()).build();
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

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(1, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		UniqueStored storedFromSearch = (UniqueStored) storedSearchResult.getResults().getFirst();

		UniqueStored stored = (UniqueStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		ReCreateEvent event = (ReCreateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());


		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setStillCheckedOut(false).setItemCheckedOut(item.getId().toHexString()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemWholeCheckout resultingCheckout = (ItemWholeCheckout) checkoutSearch.getResults().getFirst();

		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckInTransaction());
		assertEquals(details, resultingCheckout.getCheckInDetails());
	}


	//TODO:: fail - amount list - amount - to stored (not existing)
	//TODO:: Fail - amount - No 'to' given
	//TODO:: Fail - amount - 'to block' given for list
	//TODO:: fail - whole - no to block given
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Checkin Part">
	//TODO:: When we have this implemented
//</editor-fold>
//<editor-fold desc="Apply- Checkin Loss">
	//TODO:: When we have this implemented
//</editor-fold>
//<editor-fold desc="Apply- Checkout Amount">

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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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

		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId().toHexString()));
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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

		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId().toHexString()));
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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

		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId().toHexString()));
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot checkout an amount from a unique type.", e.getMessage());
	}

	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Checkout Whole">

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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 0);

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(initialStoredId));
		assertFalse(storedHistory.isEmpty());
		DeleteEvent event = (DeleteEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());

		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId().toHexString()));
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 0);

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(initialStoredId));
		assertFalse(storedHistory.isEmpty());
		DeleteEvent event = (DeleteEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());

		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId().toHexString()));
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 0);

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(initialStoredId));
		assertFalse(storedHistory.isEmpty());
		DeleteEvent event = (DeleteEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());

		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId().toHexString()));
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 0);

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(initialStoredId));
		assertFalse(storedHistory.isEmpty());
		DeleteEvent event = (DeleteEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());

		SearchResult<ItemCheckout> checkoutSearch = this.checkoutService.search(DEFAULT_TEST_DB_NAME, new ItemCheckoutSearch().setItemCheckedOut(item.getId().toHexString()));
		assertEquals(checkoutSearch.getNumResults(), 1);
		ItemWholeCheckout resultingCheckout = (ItemWholeCheckout) checkoutSearch.getResults().getFirst();

		assertEquals(details, resultingCheckout.getCheckoutDetails());
		assertEquals(appliedTransaction.getId(), resultingCheckout.getCheckOutTransaction());
		assertEquals(initialStored, resultingCheckout.getCheckedOut());
	}

	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Set Amount">
	@Test
	public void applySetAmountSuccessBulkNotInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
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

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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
			.storageBlock(item.getStorageBlocks().getFirst())
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);


		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(6, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
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


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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
			.storageBlock(item.getStorageBlocks().getFirst())
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);


		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(6, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
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

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
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
			.block(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot add an amount to a unique item.", e.getMessage());
	}

	@Test
	public void applySetAmountFailUniqueSingle() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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

		ValidationException e = assertThrows(ValidationException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Storage block " + badBlockId + " not used to hold this item ("+item.getId()+").", e.getMessage());
	}

	@Test
	public void applySetAmountFailBulkStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		ObjectId badStoredId = new ObjectId();
		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.stored(badStoredId)
			.block(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
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
			.block(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Could not find Stored with id " + badStoredId, e.getMessage());
	}

	@Test
	public void applySetAmountFailAmtListBadStoredNotInBlock() {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ObjectId origBlock = item.getStorageBlocks().getFirst();
		ObjectId otherBlockId = this.storageBlockService.add(DEFAULT_TEST_DB_NAME, StorageBlock.builder().label(FAKER.location().building()).build(), entity).getId();

		item.getStorageBlocks().add(otherBlockId);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		AmountStored originalStored = AmountStored.builder()
			.item(item.getId())
			.storageBlock(origBlock)
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.build();
		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.stored(originalStored.getId())
			.block(otherBlockId)
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored given does not exist in block.", e.getMessage());
	}

	@Test
	public void applySetAmountFailAmtListNewEntry() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		ItemStoredTransaction preApplyTransaction = SetAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.block(item.getStorageBlocks().getFirst())
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Must specify a stored to set the amount of.", e.getMessage());
	}

	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Subtract Amount">
	@Test
	public void applySubtractAmountSuccessBulkBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			AmountStored.builder()
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.item(item.getId())
				.storageBlock(item.getStorageBlocks().getFirst())
				.build(),
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(item.getStorageBlocks().getFirst())
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(0, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applySubtractAmountSuccessBulkStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		AmountStored initialStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(item.getStorageBlocks().getFirst())
			.fromStored(initialStored.getId())
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(0, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applySubtractAmountSuccessList() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		AmountStored initialStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(item.getStorageBlocks().getFirst())
			.fromStored(initialStored.getId())
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(1, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(1, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(0, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats


		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored storedFromSearch = (AmountStored) storedSearchResult.getResults().getFirst();

		AmountStored stored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().stream().findFirst().get());
		assertEquals(storedFromSearch, stored);
		assertEquals(Quantities.getQuantity(0, item.getUnit()), stored.getAmount());

		SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(stored.getId()));
		assertFalse(storedHistory.isEmpty());
		UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
		assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
		assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
	}

	@Test
	public void applySubtractAmountFailUniqueMulti() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);

		UniqueStored initialStored = UniqueStored.builder()
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(item.getStorageBlocks().getFirst())
			.fromStored(initialStored.getId())
			.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot subtract an amount from a unique item.", e.getMessage());
	}

	@Test
	public void applySubtractAmountFailUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);

		UniqueStored initialStored = UniqueStored.builder()
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(item.getStorageBlocks().getFirst())
			.fromStored(initialStored.getId())
			.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot subtract an amount from a unique item.", e.getMessage());
	}

	@Test
	public void applySubtractAmountFailBulkSubMoreThanHeld() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		AmountStored initialStored = AmountStored.builder()
			.amount(Quantities.getQuantity(2, item.getUnit()))
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(item.getStorageBlocks().getFirst())
			.fromStored(initialStored.getId())
			.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Resulting amount less than zero. (subtracting 5 units from 2 units resulting in -3 units)", e.getMessage());
	}

	@Test
	public void applySubtractAmountFailAmtListSubMoreThanHeld() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		AmountStored initialStored = AmountStored.builder()
			.amount(Quantities.getQuantity(2, item.getUnit()))
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(item.getStorageBlocks().getFirst())
			.fromStored(initialStored.getId())
			.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Resulting amount less than zero. (subtracting 5 units from 2 units resulting in -3 units)", e.getMessage());
	}

	@Test
	public void applySubtractAmountFailBulkMismatchedBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		StorageBlock newBlock = StorageBlock.builder().label(FAKER.location().building()).build();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, newBlock, entity);
		item.getStorageBlocks().add(newBlock.getId());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		AmountStored initialStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		AmountStored secondStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(newBlock.getId())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			secondStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(newBlock.getId())
			.fromStored(initialStored.getId())
			.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored id in transaction not the id of stored found.", e.getMessage());
	}

	@Test
	public void applySubtractAmountFailBulkMismatchedStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);

		StorageBlock newBlock = StorageBlock.builder().label(FAKER.location().building()).build();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, newBlock, entity);
		item.getStorageBlocks().add(newBlock.getId());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		AmountStored initialStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		AmountStored secondStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(newBlock.getId())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			secondStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(item.getStorageBlocks().getFirst())
			.fromStored(secondStored.getId())
			.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored id in transaction not the id of stored found.", e.getMessage());
	}

	@Test
	public void applySubtractAmountFailListMismatchedBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		StorageBlock newBlock = StorageBlock.builder().label(FAKER.location().building()).build();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, newBlock, entity);
		item.getStorageBlocks().add(newBlock.getId());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		AmountStored initialStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		AmountStored secondStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(newBlock.getId())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			secondStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(newBlock.getId())
			.fromStored(initialStored.getId())
			.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored retrieved not in specified block.", e.getMessage());
	}

	@Test
	public void applySubtractAmountFailListMismatchedStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		StorageBlock newBlock = StorageBlock.builder().label(FAKER.location().building()).build();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, newBlock, entity);
		item.getStorageBlocks().add(newBlock.getId());
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		AmountStored initialStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);
		AmountStored secondStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(newBlock.getId())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			secondStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(item.getStorageBlocks().getFirst())
			.fromStored(secondStored.getId())
			.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Stored retrieved not in specified block.", e.getMessage());
	}

	@Test
	public void applySubtractAmountFailNullStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);

		AmountStored initialStored = AmountStored.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.item(item.getId())
			.storageBlock(item.getStorageBlocks().getFirst())
			.build();
		this.storedService.add(
			DEFAULT_TEST_DB_NAME,
			initialStored,
			entity
		);

		ItemStoredTransaction preApplyTransaction = SubAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(item.getStorageBlocks().getFirst())
			.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Must specify the stored item we are subtracting from.", e.getMessage());
	}

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

	@Test
	public void applyTransferAmountSuccessBulkNotInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(firstBlock)
			.toBlock(secondBlock)
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(2, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(2, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 2);
		//'first' and 'second' are flipped here
		AmountStored firstStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(0);
		AmountStored secondStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(1);

		{
			AmountStored firstStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);
			assertEquals(Quantities.getQuantity(5, item.getUnit()), firstStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
		{
			AmountStored secondStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getLast());
			assertEquals(secondStoredFromSearch, secondStored);
			assertEquals(Quantities.getQuantity(0, item.getUnit()), secondStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(secondStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}

	@Test
	public void applyTransferAmountSuccessBulkInBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.build(),
			entity
		).getId();
		ObjectId destinationStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(secondBlock)
				.amount(Quantities.getQuantity(0, item.getUnit()))
				.build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(firstBlock)
			.toBlock(secondBlock)
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(2, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(2, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 2);
		//'first' and 'second' are flipped here
		AmountStored firstStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(0);
		AmountStored secondStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(1);

		{
			AmountStored firstStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);
			assertEquals(Quantities.getQuantity(5, item.getUnit()), firstStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
		{
			AmountStored secondStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getLast());
			assertEquals(secondStoredFromSearch, secondStored);
			assertEquals(Quantities.getQuantity(0, item.getUnit()), secondStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(secondStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}

	@Test
	public void applyTransferAmountSuccessBulkFromBlockToStored() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.build(),
			entity
		).getId();
		ObjectId destinationStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(secondBlock)
				.amount(Quantities.getQuantity(0, item.getUnit()))
				.build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(firstBlock)
			.toBlock(secondBlock)
			.toStored(destinationStoredId)
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(2, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(2, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 2);
		//'first' and 'second' are flipped here
		AmountStored firstStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(0);
		AmountStored secondStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(1);

		{
			AmountStored firstStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);
			assertEquals(Quantities.getQuantity(5, item.getUnit()), firstStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
		{
			AmountStored secondStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getLast());
			assertEquals(secondStoredFromSearch, secondStored);
			assertEquals(Quantities.getQuantity(0, item.getUnit()), secondStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(secondStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}

	@Test
	public void applyTransferAmountSuccessBulkFromStoredToBlock() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.build(),
			entity
		).getId();
		ObjectId destinationStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(secondBlock)
				.amount(Quantities.getQuantity(0, item.getUnit()))
				.build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(firstBlock)
			.fromStored(initialStoredId)
			.toBlock(secondBlock)
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(2, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(2, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 2);
		//'first' and 'second' are flipped here
		AmountStored firstStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(0);
		AmountStored secondStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(1);

		{
			AmountStored firstStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);
			assertEquals(Quantities.getQuantity(5, item.getUnit()), firstStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
		{
			AmountStored secondStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getLast());
			assertEquals(secondStoredFromSearch, secondStored);
			assertEquals(Quantities.getQuantity(0, item.getUnit()), secondStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(secondStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}

	@Test
	public void applyTransferAmountSuccessList() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.build(),
			entity
		).getId();
		ObjectId destinationStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(secondBlock)
				.amount(Quantities.getQuantity(0, item.getUnit()))
				.build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(firstBlock)
			.fromStored(initialStoredId)
			.toBlock(secondBlock)
			.toStored(destinationStoredId)
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(2, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(2, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 2);
		//'first' and 'second' are flipped here
		AmountStored firstStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(0);
		AmountStored secondStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(1);

		{
			AmountStored firstStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);
			assertEquals(Quantities.getQuantity(5, item.getUnit()), firstStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
		{
			AmountStored secondStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getLast());
			assertEquals(secondStoredFromSearch, secondStored);
			assertEquals(Quantities.getQuantity(0, item.getUnit()), secondStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(secondStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}

	@Test
	public void applyTransferAmountSuccessListToNonExistent() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(firstBlock)
			.fromStored(initialStoredId)
			.toBlock(secondBlock)
			.build();
		
		AppliedTransaction appliedTransaction = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);

		assertEquals(entity.getId(), appliedTransaction.getEntity());
		assertEquals(item.getId(), appliedTransaction.getInventoryItem());
		assertEquals(2, appliedTransaction.getAffectedStored().size());
		assertEquals(preApplyTransaction, appliedTransaction.getTransaction());
		assertTrue(appliedTransaction.getTimestamp().isBefore(ZonedDateTime.now()));

		assertEquals(2, appliedTransaction.getPostApplyResults().getStats().getNumStored());
		assertEquals(Quantities.getQuantity(5, item.getUnit()), appliedTransaction.getPostApplyResults().getStats().getTotal());
		//TODO:: storage block stats

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 2);
		//'first' and 'second' are flipped here
		AmountStored firstStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(0);
		AmountStored secondStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(1);

		{
			AmountStored firstStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);
			assertEquals(Quantities.getQuantity(5, item.getUnit()), firstStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
		{
			AmountStored secondStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getLast());
			assertEquals(secondStoredFromSearch, secondStored);
			assertEquals(Quantities.getQuantity(0, item.getUnit()), secondStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(secondStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}


	@Test
	public void applyTransferAmountFailUniqueMulti() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_MULTI, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(firstBlock)
			.toBlock(secondBlock)
			.build();


		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot subtract an amount from a unique item.", e.getMessage());
	}

	@Test
	public void applyTransferAmountFailUniqueSingle() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.UNIQUE_SINGLE, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(5, item.getUnit()))
			.fromBlock(firstBlock)
			.toBlock(secondBlock)
			.build();


		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Cannot subtract an amount from a unique item.", e.getMessage());
	}

	@Test
	public void applyTransferAmountFailBulkNotEnoughToTransfer() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(6, item.getUnit()))
			.fromBlock(firstBlock)
			.toBlock(secondBlock)
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Resulting amount less than zero. (subtracting 6 units from 5 units resulting in -1 units)", e.getMessage());
	}

	@Test
	public void applyTransferAmountFailAmtListNotEnoughToTransfer() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.AMOUNT_LIST, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(6, item.getUnit()))
			.fromBlock(firstBlock)
			.fromStored(initialStoredId)
			.toBlock(secondBlock)
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("Resulting amount less than zero. (subtracting 6 units from 5 units resulting in -1 units)", e.getMessage());
	}

	@Test
	public void applyTransferAmountFailBulkMismatchedStoredId() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.build(),
			entity
		).getId();
		ObjectId destinationStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(secondBlock)
				.amount(Quantities.getQuantity(0, item.getUnit()))
				.build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(6, item.getUnit()))
			.fromBlock(firstBlock)
			.fromStored(destinationStoredId)
			.toBlock(secondBlock)
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("From Stored retrieved not in specified block.", e.getMessage());
	}

	@Test
	public void applyTransferAmountFailBulkMismatchedBlockTo() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
				.amount(Quantities.getQuantity(5, item.getUnit()))
				.build(),
			entity
		).getId();
		ObjectId destinationStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(secondBlock)
				.amount(Quantities.getQuantity(0, item.getUnit()))
				.build(),
			entity
		).getId();

		ItemStoredTransaction preApplyTransaction = TransferAmountTransaction.builder()
			.amount(Quantities.getQuantity(6, item.getUnit()))
			.fromBlock(firstBlock)
			.toBlock(secondBlock)
			.toStored(initialStoredId)
			.build();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity));
		assertEquals("To Stored retrieved not in specified block.", e.getMessage());
	}

	//TODO:: any more?
//</editor-fold>
//<editor-fold desc="Apply- Transfer Whole">

	@Test
	public void applyTransferWholeSuccessBulk() throws Exception {
		InteractingEntity entity = this.getTestUserService().getTestUser();
		InventoryItem item = setupItem(StorageType.BULK, entity);
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
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

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored firstStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(0);

		{
			AmountStored firstStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);
			assertEquals(Quantities.getQuantity(5, item.getUnit()), firstStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
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
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, AmountStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
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

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		AmountStored firstStoredFromSearch = (AmountStored) storedSearchResult.getResults().get(0);

		{
			AmountStored firstStored = (AmountStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);
			assertEquals(Quantities.getQuantity(5, item.getUnit()), firstStored.getAmount());

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
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
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, UniqueStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
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

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		UniqueStored firstStoredFromSearch = (UniqueStored) storedSearchResult.getResults().get(0);

		{
			UniqueStored firstStored = (UniqueStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
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
		ObjectId firstBlock = item.getStorageBlocks().getFirst();
		ObjectId secondBlock = this.storageBlockService.add(
			DEFAULT_TEST_DB_NAME,
			StorageBlock.builder().label(FAKER.location().building()).build(),
			entity
		).getId();
		item.getStorageBlocks().add(secondBlock);
		this.inventoryItemService.update(DEFAULT_TEST_DB_NAME, item, entity);

		ObjectId initialStoredId = this.storedService.add(
			DEFAULT_TEST_DB_NAME, UniqueStored.builder()
				.item(item.getId())
				.storageBlock(firstBlock)
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

		SearchResult<Stored> storedSearchResult = this.storedService.search(DEFAULT_TEST_DB_NAME, new StoredSearch().setInventoryItemId(item.getId().toHexString()));
		assertEquals(storedSearchResult.getNumResults(), 1);
		UniqueStored firstStoredFromSearch = (UniqueStored) storedSearchResult.getResults().get(0);

		{
			UniqueStored firstStored = (UniqueStored) this.storedService.get(DEFAULT_TEST_DB_NAME, appliedTransaction.getAffectedStored().getFirst());
			assertEquals(firstStoredFromSearch, firstStored);

			SearchResult<ObjectHistoryEvent> storedHistory = this.storedService.getHistoryService().search(DEFAULT_TEST_DB_NAME, new HistorySearch().setObjectId(firstStored.getId()));
			assertFalse(storedHistory.isEmpty());
			UpdateEvent event = (UpdateEvent) storedHistory.getResults().getFirst();
			assertTrue(event.getDetails().containsKey(ITEM_TRANSACTION.name()));
			assertEquals(appliedTransaction.getId(), ((ItemTransactionDetail) event.getDetails().get(ITEM_TRANSACTION.name())).getInventoryItemTransaction());
		}
	}


	//TODO:: any more?
//</editor-fold>
	//<editor-fold desc="Post transaction processing">
	//TODO:: fix these, inconsistent and flaky
//	@Test
//	public void applyTransactionPostProcessNoChange() throws Exception {
//		InteractingEntity entity = this.getTestUserService().getTestUser();
//		InventoryItem item = setupItem(StorageType.BULK, entity);
//
//		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
//			.amount(Quantities.getQuantity(5, item.getUnit()))
//			.toBlock(item.getStorageBlocks().getFirst())
//			.build();
//
//		this.clearQueues();
//		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
//		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);
//
//		List<EventNotificationWrapper> messages = this.assertMessages(EventType.CREATE, EventType.UPDATE);
//		//TODO:: verify messages
//	}
//
//	@Test
//	public void applyTransactionPostProcessStoredLowStock() throws Exception {
//		InteractingEntity entity = this.getTestUserService().getTestUser();
//		InventoryItem item = setupItem(StorageType.BULK, entity);
//
//		AmountStored originalStored = AmountStored.builder()
//			.item(item.getId())
//			.storageBlock(item.getStorageBlocks().getFirst())
//			.amount(Quantities.getQuantity(5, item.getUnit()))
//			.lowStockThreshold(Quantities.getQuantity(20, item.getUnit()))
//			.build();
//		this.storedService.add(DEFAULT_TEST_DB_NAME, originalStored, entity);
//
//		ItemStoredTransaction preApplyTransaction = AddAmountTransaction.builder()
//			.amount(Quantities.getQuantity(5, item.getUnit()))
//			.toBlock(item.getStorageBlocks().getFirst())
//			.build();
//
//		this.clearQueues();
//		ObjectId appliedTransactionId = this.appliedTransactionService.apply(DEFAULT_TEST_DB_NAME, null, item, preApplyTransaction, entity);
//		AppliedTransaction appliedTransaction = this.appliedTransactionService.get(DEFAULT_TEST_DB_NAME, appliedTransactionId);
//
//		List<EventNotificationWrapper> messages = this.assertMessages(EventType.UPDATE, EventType.UPDATE, EventType.UPDATE, EventType.ITEM_LOW_STOCK);
//		//TODO:: assert events, notification state
//	}


	//TODO:: low stock (stored)
	//TODO:: low stock (item)
	//TODO:: expiry warn alert
	//TODO:: expired alert
	//</editor-fold>
}