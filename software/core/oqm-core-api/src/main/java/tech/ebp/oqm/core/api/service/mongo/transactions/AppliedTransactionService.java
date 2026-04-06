package tech.ebp.oqm.core.api.service.mongo.transactions;

import com.mongodb.client.ClientSession;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.history.details.ItemTransactionDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.rest.search.AppliedTransactionSearch;
import tech.ebp.oqm.core.api.service.ItemStatsService;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.MongoObjectService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.*;
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;

import java.util.*;

import static tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType.*;

/**
 * Service to handle applying transactions to items stored, and keeping track of what transactions have been applied.
 */
@Slf4j
@Named("ItemStoredTransactionService")
@ApplicationScoped
public class AppliedTransactionService extends MongoObjectService<AppliedTransaction, AppliedTransactionSearch, CollectionStats> {
	
	@Inject
	StoredService storedService;
	
	@Inject
	ItemStatsService itemStatsService;
	
	@Inject
	ItemCheckoutService itemCheckoutService;
	
	@Getter
	Map<TransactionType, TransactionApplier> appliers = new HashMap<>();
	
	public AppliedTransactionService() {
		super(AppliedTransaction.class);
	}
	
	@PostConstruct
	void setupAppliers() {
		appliers.put(ADD_AMOUNT, new AddAmountTransactionApplier(this.storedService));
		appliers.put(ADD_WHOLE, new AddWholeTransactionApplier(this.storedService));
		appliers.put(CHECKIN_FULL, new CheckinFullTransactionApplier(this.storedService, this.itemCheckoutService));
		appliers.put(CHECKIN_PART, new CheckinPartTransactionApplier(this.storedService, this.itemCheckoutService));
		appliers.put(CHECKOUT_AMOUNT, new CheckoutAmountTransactionApplier(this.storedService, this.itemCheckoutService));
		appliers.put(CHECKOUT_WHOLE, new CheckoutWholeTransactionApplier(this.storedService, this.itemCheckoutService));
		appliers.put(SET_AMOUNT, new SetAmountTransactionApplier(this.storedService));
		appliers.put(SUBTRACT_AMOUNT, new SubtractAmountTransactionApplier(this.storedService));
		appliers.put(SUBTRACT_WHOLE, new SubtractWholeTransactionApplier(this.storedService));
		appliers.put(TRANSFER_AMOUNT, new TransferAmountTransactionApplier(this.storedService));
		appliers.put(TRANSFER_WHOLE, new TransferWholeTransactionApplier(this.storedService));
	}
	
	/**
	 * Applies the transaction given.
	 *
	 * @param oqmDbIdOrName
	 * @param cs
	 * @param inventoryItem
	 * @param itemStoredTransaction
	 * @param interactingEntity
	 *
	 * @return
	 */
	public <T extends ItemStoredTransaction> AppliedTransaction apply(
		String oqmDbIdOrName,
		ClientSession cs,
		@NotNull InventoryItem inventoryItem,
		@Valid T itemStoredTransaction,
		InteractingEntity interactingEntity,
		HistoryDetail... details
	) throws Exception {
		try (MongoSessionWrapper csw = new MongoSessionWrapper(cs, this)) {
			return csw.runTransaction(()->{
				log.info("Applying {} transaction ", itemStoredTransaction.getType());
				log.debug("Transaction: {}", itemStoredTransaction);
				final ObjectId appliedTransactionId = new ObjectId();
				HistoryDetail[] historyDetails;
				{
					List<HistoryDetail> deetsCollection = new ArrayList<>();
					deetsCollection.addAll(List.of(details));
					deetsCollection.add(new ItemTransactionDetail(appliedTransactionId));
					historyDetails = deetsCollection.toArray(new HistoryDetail[0]);
				}
				AppliedTransaction.AppliedTransactionBuilder<?, ?> appliedTransactionBuilder = AppliedTransaction.builder()
																				 .id(appliedTransactionId)
																				 .inventoryItem(inventoryItem.getId())
																				 .transaction(itemStoredTransaction)
																				 .entity(interactingEntity.getId());
				LinkedHashSet<Stored> affectedStored = new LinkedHashSet<>();
				
				
				//noinspection rawtypes
				TransactionApplier applier = this.getAppliers().get(itemStoredTransaction.getType());
				if (applier == null) {
					throw new IllegalArgumentException("Invalid or unsupported transaction type given.");
				}
				//noinspection unchecked
				applier.apply(
					oqmDbIdOrName,
					inventoryItem,
					itemStoredTransaction,
					appliedTransactionId,
					interactingEntity,
					affectedStored,
					historyDetails,
					csw.getClientSession()
				);
				
				appliedTransactionBuilder.affectedStored(new LinkedHashSet<>(affectedStored.stream().map(Stored::getId).toList()));
				appliedTransactionBuilder.postApplyResults(this.itemStatsService.postTransactionProcess(
					oqmDbIdOrName,
					csw.getClientSession(),
					inventoryItem,
					appliedTransactionId,
					affectedStored,
					interactingEntity,
					historyDetails
				));
				
				AppliedTransaction appliedTransaction = appliedTransactionBuilder.build();
				appliedTransaction = this.add(oqmDbIdOrName, appliedTransaction);
				
				log.info("Completed transaction. Applied transaction id: {}", appliedTransaction.getId());
				log.debug("Applied transaction: {}", appliedTransaction);
				
				if (!appliedTransaction.getId().equals(appliedTransactionId)) {
					log.warn(
						"New id after adding applied transaction DIFFERENT from one generated previously; Original: {} / From Mongo: {}",
						appliedTransactionId,
						appliedTransaction.getId()
					);
				}
				
				return appliedTransaction;
			});
		} catch(Exception e) {
			log.error("Failed to apply transaction: ", e);
			throw e;
		}
	}
	
	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return null;//TODO
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return AppliedTransaction.CUR_SCHEMA_VERSION;
	}
}
