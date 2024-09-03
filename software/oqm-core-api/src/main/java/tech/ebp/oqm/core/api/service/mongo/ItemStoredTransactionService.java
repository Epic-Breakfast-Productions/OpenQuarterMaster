package tech.ebp.oqm.core.api.service.mongo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.rest.search.AppliedTransactionSearch;

@Slf4j
@Named("ItemStoredTransactionService")
@ApplicationScoped
public class ItemStoredTransactionService extends MongoObjectService<AppliedTransaction, AppliedTransactionSearch, CollectionStats> {

	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	StoredService storedService;

	public ItemStoredTransactionService() {
		super(AppliedTransaction.class);
	}


	public ObjectId apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		ItemStoredTransaction itemStoredTransaction
	){
		AppliedTransaction.Builder<?,?> appliedTransactionBuilder = AppliedTransaction.builder()
			.inventoryItem(inventoryItem.getId())
			.transaction(itemStoredTransaction);

		switch (inventoryItem.getStorageType()){
			case BULK -> {
				//TODO Only allow one stored to exist
			}
			case AMOUNT_LIST -> {
				//TODO: if new, add to stored
			}
			case UNIQUE_MULTI -> {
				//TODO:: add to stored
			}
			case UNIQUE_SINGLE -> {
				//TODO:: Only allow one stored to exist
			}
		}

		ObjectId newId = this.add(oqmDbIdOrName, appliedTransactionBuilder.build());
		return newId;
	}

	public ObjectId apply(
		String oqmDbIdOrName,
		ObjectId inventoryItem,
		ItemStoredTransaction itemStoredTransaction
	){
		return this.apply(oqmDbIdOrName, this.inventoryItemService.get(oqmDbIdOrName, inventoryItem), itemStoredTransaction);
	}

	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return null;
	}
}
