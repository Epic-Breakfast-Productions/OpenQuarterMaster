
package tech.ebp.oqm.core.api.service.mongo.transactions.transactionApplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
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
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.model.messaging.EventNotificationWrapper;
import tech.ebp.oqm.core.api.service.mongo.transactions.AppliedTransactionService;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;
import tech.ebp.oqm.core.api.testResources.data.InventoryItemTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.data.StorageBlockTestObjectCreator;
import tech.ebp.oqm.core.api.testResources.testClasses.KafkaTest;
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
abstract class AppliedTransactionServiceTest extends MongoObjectServiceTest<AppliedTransaction, AppliedTransactionService> implements KafkaTest {
	
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
	
	@Getter
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
	
	
	protected InventoryItem setupItem(StorageType storageType, InteractingEntity entity) {
		StorageBlock storageBlock = storageBlockTestObjectCreator.getTestObject();
		this.storageBlockService.add(DEFAULT_TEST_DB_NAME, storageBlock, entity);
		InventoryItem item = this.itemTestObjectCreator.getTestObject().setStorageType(storageType);
		item.getStorageBlocks().add(storageBlock.getId());
		this.inventoryItemService.add(DEFAULT_TEST_DB_NAME, item, entity);
		return item;
	}
	
	protected void clearQueues() {
		this.kafkaCompanion
			.topics()
			.delete(HistoryEventNotificationService.ALL_EVENT_TOPIC)
		//			.clear(HistoryEventNotificationService.ALL_EVENT_TOPIC)
		;
	}
	
	//TODO:: this with all following tests
	protected List<EventNotificationWrapper> assertMessages(
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
		
		
		try {
			
			ConsumerTask<String, String> messagesFromAll = this.kafkaCompanion.consumeStrings().fromTopics(
				HistoryEventNotificationService.ALL_EVENT_TOPIC
				, expectedEvents.length
			);
			messagesFromAll.awaitCompletion();
			assertEquals(expectedEvents.length, messagesFromAll.count());
			
			List<EventNotificationWrapper> eventWrappers = messagesFromAll.stream()
															   .map(record->{
																   try {
																	   return ObjectUtils.OBJECT_MAPPER.readValue(record.value(), EventNotificationWrapper.class);
																   } catch(JsonProcessingException e) {
																	   throw new RuntimeException(e);
																   }
															   })
															   .collect(Collectors.toList());
			log.info("Found messages for transaction: {}", eventWrappers);
			
			assertEquals(expectedEvents.length, eventWrappers.size());
			
			for (int i = 0; i < expectedEvents.length; i++) {
				EventNotificationWrapper curWrapper = eventWrappers.get(i);
				EventType expectedType = expectedEvents[i];
				
				assertEquals(expectedType, curWrapper.getEvent().getType(), "Unexpected type of transaction at index " + i);
			}
			
			return eventWrappers;
		} catch(UnknownTopicOrPartitionException e) {
			log.warn("Failed to clear queues", e);
			throw e;
		}
	}
	
	//<editor-fold desc="Apply- Checkin Part">
	//TODO:: When we have this implemented
	//</editor-fold>
	//<editor-fold desc="Apply- Checkin Loss">
	//TODO:: When we have this implemented
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