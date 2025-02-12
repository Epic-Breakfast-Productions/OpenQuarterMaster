package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferWholeTransaction;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.units.indriya.quantity.Quantities;

import java.util.LinkedHashSet;
import java.util.Set;

public class TransferWholeTransactionApplier extends TransactionApplier<TransferWholeTransaction> {

	public TransferWholeTransactionApplier(StoredService storedService) {
		super(storedService);
	}

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.TRANSFER_WHOLE;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		TransferWholeTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		Stored toTransfer;
		switch (inventoryItem.getStorageType()) {
			case BULK, UNIQUE_SINGLE -> {
				toTransfer = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transaction.getFromBlock(), Stored.class);

				if (transaction.getStoredToTransfer() != null && !transaction.getStoredToTransfer().equals(toTransfer.getId())) {
					throw new IllegalArgumentException("Stored id given mismatched id from gotten stored.");
				}
			}
			case AMOUNT_LIST, UNIQUE_MULTI -> {
				toTransfer = this.getStoredService().get(oqmDbIdOrName, cs, transaction.getStoredToTransfer());

				if (!transaction.getFromBlock().equals(toTransfer.getStorageBlock())) {
					throw new IllegalArgumentException("Stored found not in specified block.");
				}
			}
			default ->
				throw new IllegalArgumentException("Unsupported storage type. This should never happen.");
		}

		if (!inventoryItem.getId().equals(toTransfer.getItem())) {
			throw new IllegalArgumentException("Stored is not associated with the item.");
		}

		toTransfer.setStorageBlock(transaction.getToBlock());

		affectedStored.add(toTransfer);
		this.getStoredService().update(oqmDbIdOrName, cs, toTransfer, interactingEntity, historyDetails);
	}
}
