package tech.ebp.oqm.core.api.service.mongo.transactions.appliers;

import com.mongodb.client.ClientSession;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set.SetAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.units.indriya.quantity.Quantities;

import java.util.LinkedHashSet;
import java.util.Set;

public class SubtractAmountTransactionApplier extends TransactionApplier<SubAmountTransaction> {

	public SubtractAmountTransactionApplier(StoredService storedService) {
		super(storedService);
	}

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.SUBTRACT_AMOUNT;
	}

	@Override
	public void apply(
		String oqmDbIdOrName,
		InventoryItem inventoryItem,
		SubAmountTransaction transaction,
		ObjectId appliedTransactionId,
		InteractingEntity interactingEntity,
		Set<Stored> affectedStored,
		HistoryDetail[] historyDetails,
		ClientSession cs
	) {
		AmountStored stored;
		switch (inventoryItem.getStorageType()) {
			case BULK -> {
				stored = this.getStoredService().getSingleStoredForItemBlock(oqmDbIdOrName, cs, inventoryItem.getId(), transaction.getFromBlock(), AmountStored.class);
			}
			case AMOUNT_LIST -> {
				stored = (AmountStored) this.getStoredService().get(oqmDbIdOrName, cs, transaction.getFromStored());
			}
			default -> {
				throw new IllegalArgumentException("Cannot subtract an amount from a unique item.");
			}
		}

		if (!inventoryItem.getId().equals(stored.getItem())) {
			throw new IllegalArgumentException("Stored is not associated with the item.");
		}

		if (transaction.getFromStored() != null && !stored.getId().equals(transaction.getFromStored())) {
			throw new IllegalArgumentException("Stored id in transaction not the id of stored found.");
		}
		if (!stored.getStorageBlock().equals(transaction.getFromBlock())) {
			throw new IllegalArgumentException("Stored retrieved not in specified block.");
		}
		stored.subtract(transaction.getAmount());

		affectedStored.add(stored);
		this.getStoredService().update(oqmDbIdOrName, cs, stored, interactingEntity, historyDetails);

	}
}
