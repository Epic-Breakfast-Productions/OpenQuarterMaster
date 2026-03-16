package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.service.mongo.StoredService;

import java.util.Set;

public class AddWholeTransactionApplier extends TransactionApplier<AddWholeTransaction> {

	public AddWholeTransactionApplier(StoredService storedService) {
		super(storedService);
	}

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.ADD_WHOLE;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		AddWholeTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		Stored stored = transaction.getToAdd();
		
		if(stored.getItem() == null) {
			stored.setItem(inventoryItem.getId());
		}
		
		if(stored.getStorageBlock() == null) {
			stored.setStorageBlock(transaction.getToBlock());
		}

		if (!inventoryItem.getId().equals(stored.getItem())) {
			throw new IllegalArgumentException("Stored given is not associated with item.");
		}

		if (inventoryItem.getStorageType() == StorageType.BULK) {
			throw new IllegalArgumentException("Cannot add whole item to a bulk storage typed item.");
		}

		if (!transaction.getToBlock().equals(stored.getStorageBlock())) {
			throw new IllegalArgumentException("To Block given does not match block marked in stored.");
		}

		this.getStoredService().add(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);
		affectedStored.add(stored);
	}
}
